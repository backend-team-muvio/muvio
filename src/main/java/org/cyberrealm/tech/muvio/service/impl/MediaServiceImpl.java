package org.cyberrealm.tech.muvio.service.impl;

import static org.cyberrealm.tech.muvio.common.Constants.BACK_OFF;
import static org.cyberrealm.tech.muvio.common.Constants.LANGUAGE_EN;
import static org.cyberrealm.tech.muvio.common.Constants.ONE_HUNDRED;
import static org.cyberrealm.tech.muvio.common.Constants.RATING;
import static org.cyberrealm.tech.muvio.common.Constants.SIX;
import static org.cyberrealm.tech.muvio.common.Constants.TEN;
import static org.cyberrealm.tech.muvio.common.Constants.THREE;
import static org.cyberrealm.tech.muvio.common.Constants.ZERO;

import com.mongodb.MongoSocketReadTimeoutException;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.cyberrealm.tech.muvio.dto.MainPageInfoDto;
import org.cyberrealm.tech.muvio.dto.MediaBaseDto;
import org.cyberrealm.tech.muvio.dto.MediaDto;
import org.cyberrealm.tech.muvio.dto.MediaDtoFromDb;
import org.cyberrealm.tech.muvio.dto.MediaDtoWithCast;
import org.cyberrealm.tech.muvio.dto.MediaDtoWithCastFromDb;
import org.cyberrealm.tech.muvio.dto.MediaDtoWithPoints;
import org.cyberrealm.tech.muvio.dto.MediaGalleryRequestDto;
import org.cyberrealm.tech.muvio.dto.MediaVibeRequestDto;
import org.cyberrealm.tech.muvio.dto.PosterDto;
import org.cyberrealm.tech.muvio.dto.TitleDto;
import org.cyberrealm.tech.muvio.exception.EntityNotFoundException;
import org.cyberrealm.tech.muvio.exception.MediaProcessingException;
import org.cyberrealm.tech.muvio.mapper.GenreMapper;
import org.cyberrealm.tech.muvio.mapper.MediaMapper;
import org.cyberrealm.tech.muvio.model.GenreEntity;
import org.cyberrealm.tech.muvio.model.Media;
import org.cyberrealm.tech.muvio.model.Type;
import org.cyberrealm.tech.muvio.repository.ActorRepository;
import org.cyberrealm.tech.muvio.repository.MediaRepository;
import org.cyberrealm.tech.muvio.service.LocalizationMediaService;
import org.cyberrealm.tech.muvio.service.MediaService;
import org.cyberrealm.tech.muvio.service.PaginationUtil;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {
    private static final String POINTS = "points";
    private static final int MIN_TITLE_LENGTH = 3;
    private static final String MEDIA_STATISTICS = "mediaStatistics";
    private static final String RESULT_NULL = "#result == null";
    private final MediaRepository mediaRepository;
    private final LocalizationMediaService localizationMediaService;
    private final ActorRepository actorRepository;
    private final MediaMapper mediaMapper;
    private final GenreMapper genreMapper;
    private final PaginationUtil paginationUtil;

    @Override
    @Retryable(retryFor = {
            DataAccessResourceFailureException.class, MongoSocketReadTimeoutException.class
    },
            backoff = @Backoff(delay = BACK_OFF))
    public MediaDto getMediaById(String id, String lang) {
        if (isLocalizationRequired(lang)) {
            return localizationMediaService.findById(id, mediaRepository.findById(
                    id.substring(lang.length())).orElseThrow(() -> new EntityNotFoundException(
                            "There is no media with this id: " + id)));
        } else {
            return mediaMapper.toMovieDto(mediaRepository.findMovieById(id)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "There is no media with this id: " + id)));
        }
    }

    @Override
    @Retryable(retryFor = {
            DataAccessResourceFailureException.class, MongoSocketReadTimeoutException.class
    },
            backoff = @Backoff(delay = BACK_OFF))
    public Slice<MediaDtoWithPoints> getAllMediaByVibe(MediaVibeRequestDto requestDto) {
        final List<Media> mediaList = mediaRepository.getAllMediaByVibes(requestDto);
        final Set<String> categories = getCategories(requestDto.categories());
        final String lang = requestDto.lang();
        final List<MediaDtoWithPoints> mediasWithPoints;
        if (isLocalizationRequired(lang)) {
            mediasWithPoints = localizationMediaService.getAllMediaByVibe(mediaList,
                    categories, lang);
        } else {
            mediasWithPoints = mediaList.stream().map(
                    media -> mediaMapper.toMediaDtoWithPoints(media, categories)).toList();
        }
        return paginationUtil.paginateListWithOneRandomBefore(PageRequest.of(
                requestDto.page() == null ? ZERO : requestDto.page(),
                requestDto.size() == null ? TEN : requestDto.size(),
                Sort.by(POINTS).descending().and(Sort.by(RATING).descending())), mediasWithPoints);
    }

    @Override
    @Retryable(retryFor = {
            DataAccessResourceFailureException.class, MongoSocketReadTimeoutException.class
    },
            backoff = @Backoff(delay = BACK_OFF))
    public Slice<MediaBaseDto> getAllForGallery(MediaGalleryRequestDto requestDto,
                                                Pageable pageable) {
        final String lang = requestDto.lang();
        final List<Media> mediaForGallery = mediaRepository.getAllForGallery(requestDto, pageable);
        if (isLocalizationRequired(lang)) {
            return localizationMediaService.getAllForGalleryLocalization(
                    mediaForGallery, requestDto.title(), pageable, lang);
        } else {
            List<MediaBaseDto> listMedias = mediaForGallery.stream()
                    .map(mediaMapper::toMediaBaseDto).toList();
            return new SliceImpl<>(listMedias, pageable, !listMedias.isEmpty());
        }
    }

    @Override
    @Retryable(retryFor = {
            DataAccessResourceFailureException.class, MongoSocketReadTimeoutException.class
    },
            backoff = @Backoff(delay = BACK_OFF))
    public Set<MediaDto> getAllLuck(int size, String lang) {
        final Set<MediaDtoFromDb> allLuck = mediaRepository.getAllLuck(size);
        if (isLocalizationRequired(lang)) {
            return localizationMediaService.getAllLuck(allLuck, lang);
        } else {
            return allLuck.stream()
                    .map(mediaMapper::toMovieDto).collect(Collectors.toSet());
        }
    }

    @Transactional
    @Override
    @Retryable(retryFor = {
            DataAccessResourceFailureException.class, MongoSocketReadTimeoutException.class
    },
            backoff = @Backoff(delay = BACK_OFF))
    public Slice<MediaBaseDto> getRecommendations(int page, String lang) {
        boolean stop = true;
        final int minYear = Year.now().getValue() - THREE;
        List<Stack<MediaBaseDto>> stacks = List.of(
                fetchMedia(Type.MOVIE, GenreEntity.CRIME, minYear),
                fetchMedia(Type.MOVIE, GenreEntity.DRAMA, minYear),
                fetchMedia(Type.MOVIE, GenreEntity.COMEDY, minYear),
                fetchMedia(Type.TV_SHOW, GenreEntity.CRIME, minYear),
                fetchMedia(Type.TV_SHOW, GenreEntity.DRAMA, minYear),
                fetchMedia(Type.TV_SHOW, GenreEntity.COMEDY, minYear)
        );
        final List<MediaBaseDto> recommendations = new ArrayList<>();
        while (stop) {
            for (Stack<MediaBaseDto> stack : stacks) {
                if (!stack.isEmpty()) {
                    addNewMedia(recommendations, stack);
                } else {
                    stop = false;
                    break;
                }
            }
        }
        final PageRequest pageRequest = PageRequest.of(page, SIX);
        final Page<MediaBaseDto> mediaPage = paginationUtil
                .paginateList(pageRequest, recommendations);
        if (isLocalizationRequired(lang)) {
            return localizationMediaService.getRecommendations(mediaPage, pageRequest, lang);
        } else {
            updateDuration(mediaPage);
            mediaPage.forEach(
                    media -> {
                        media.setGenres(genreMapper.toStringGenres(media.getGenres()));
                        media.setType(mediaMapper.toCorrectType(media.getType()));
                    });
            return mediaPage.getContent().size() < SIX ? null : mediaPage;
        }
    }

    @Override
    @Retryable(retryFor = {
            DataAccessResourceFailureException.class, MongoSocketReadTimeoutException.class
    },
            backoff = @Backoff(delay = BACK_OFF))
    public Slice<MediaDtoWithCast> findMediaByTopLists(
            String topList, int page, int size, String lang) {
        final Pageable pageable = PageRequest.of(page, size, Sort.by(RATING).descending());
        final Slice<MediaDtoWithCastFromDb> media = mediaRepository
                .findByTopListsContaining(topList, pageable);
        final List<MediaDtoWithCast> mediaList;
        if (isLocalizationRequired(lang)) {
            mediaList = localizationMediaService.findMediaByTopLists(media, lang);
        } else {
            mediaList = media.stream()
                    .map(mediaMapper::toMediaDtoWithCast).toList();

        }
        return new SliceImpl<>(mediaList, pageable, !mediaList.isEmpty());
    }

    @Override
    @Retryable(retryFor = {
            DataAccessResourceFailureException.class, MongoSocketReadTimeoutException.class
    },
            backoff = @Backoff(delay = BACK_OFF))
    public List<PosterDto> getRandomPosters(int size, String lang) {
        if (isLocalizationRequired(lang)) {
            return localizationMediaService.getRandomPosters(size, lang);
        } else {
            return mediaRepository.getRandomPosters(size);
        }
    }

    @Override
    @Retryable(retryFor = {
            DataAccessResourceFailureException.class, MongoSocketReadTimeoutException.class
    },
            backoff = @Backoff(delay = BACK_OFF))
    public Slice<TitleDto> findAllTitles(Pageable pageable, String lang) {
        if (isLocalizationRequired(lang)) {
            return localizationMediaService.findAllTitles(pageable, lang);
        } else {
            return mediaRepository.findAllTitles(pageable);
        }
    }

    @Override
    @Retryable(retryFor = {
            DataAccessResourceFailureException.class, MongoSocketReadTimeoutException.class
    },
            backoff = @Backoff(delay = BACK_OFF))
    public Slice<MediaBaseDto> findByTitle(String title, Pageable pageable, String lang) {
        if (title == null || title.length() < MIN_TITLE_LENGTH) {
            throw new IllegalArgumentException("The title must contain at least 3 characters");
        }
        if (isLocalizationRequired(lang)) {
            return localizationMediaService.findByTitle(title,pageable, lang);
        } else {
            final Slice<MediaBaseDto> baseDtoSlice = Optional.ofNullable(
                            mediaRepository.findByTitle(title, pageable))
                    .filter(slice -> !slice.isEmpty())
                    .orElseThrow(() ->
                            new MediaProcessingException("Couldn't find media by title: " + title));
            baseDtoSlice.forEach(
                    media -> {
                        media.setGenres(genreMapper.toStringGenres(media.getGenres()));
                        media.setType(mediaMapper.toCorrectType(media.getType()));
                    });
            return baseDtoSlice;
        }
    }

    @Override
    @Retryable(retryFor = {
            DataAccessResourceFailureException.class, MongoSocketReadTimeoutException.class
    },
            backoff = @Backoff(delay = BACK_OFF))
    public List<MediaBaseDto> getAll(Pageable pageable, String lang) {
        final List<MediaBaseDto> mediaBaseList = mediaRepository.getAll(pageable);
        if (isLocalizationRequired(lang)) {
            return localizationMediaService.getAll(mediaBaseList, lang);
        } else {
            return mediaBaseList.stream().peek(media -> {
                media.setDuration(mediaMapper.toDuration(Integer.valueOf(media.getDuration())));
                media.setGenres(genreMapper.toStringGenres(media.getGenres()));
                media.setType(mediaMapper.toCorrectType(media.getType()));
            }).toList();
        }
    }

    @Override
    @Retryable(retryFor = {
            DataAccessResourceFailureException.class, MongoSocketReadTimeoutException.class
    },
            backoff = @Backoff(delay = BACK_OFF))
    public long count() {
        return mediaRepository.count();
    }

    @Cacheable(value = MEDIA_STATISTICS, unless = RESULT_NULL)
    @Override
    @Retryable(retryFor = {
            DataAccessResourceFailureException.class, MongoSocketReadTimeoutException.class
    },
            backoff = @Backoff(delay = BACK_OFF))
    public MainPageInfoDto getMainPageInfo() {
        return new MainPageInfoDto(count(), getGenreCount(), actorRepository.count());
    }

    private int getGenreCount() {
        return GenreEntity.values().length;
    }

    private void addNewMedia(List<MediaBaseDto> recommendations, Stack<MediaBaseDto> media) {
        while (!media.isEmpty()) {
            final MediaBaseDto candidate = media.pop();
            if (!recommendations.contains(candidate)) {
                recommendations.add(candidate);
                break;
            }
        }
    }

    private Stack<MediaBaseDto> fetchMedia(Type type, GenreEntity genre, int minYear) {
        final Pageable sortedPageRequest = PageRequest.of(ZERO, ONE_HUNDRED, Sort.by(RATING));
        final List<MediaBaseDto> content = mediaRepository
                .findMoviesByTypeGenreAndYears(type, genre, minYear, sortedPageRequest)
                .getContent();
        final Stack<MediaBaseDto> stack = new Stack<>();
        stack.addAll(content);
        return stack;
    }

    private <T extends MediaBaseDto> void updateDuration(Iterable<T> mediaList) {
        mediaList.forEach(media -> {
            if (media.getDuration() != null) {
                media.setDuration(
                        mediaMapper.toDuration(Integer.parseInt(media.getDuration())));
            }
        });
    }

    private Set<String> getCategories(Set<String> categories) {
        return categories != null
                ? categories.stream().map(String::toUpperCase).collect(Collectors.toSet())
                : Set.of();
    }

    private boolean isLocalizationRequired(String lang) {
        return lang != null && !lang.isEmpty() && !lang.equals(LANGUAGE_EN);
    }
}
