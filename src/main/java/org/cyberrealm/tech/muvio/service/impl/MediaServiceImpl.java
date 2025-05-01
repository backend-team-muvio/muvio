package org.cyberrealm.tech.muvio.service.impl;

import static org.cyberrealm.tech.muvio.common.Constants.ONE_HUNDRED;
import static org.cyberrealm.tech.muvio.common.Constants.RATING;
import static org.cyberrealm.tech.muvio.common.Constants.SIX;
import static org.cyberrealm.tech.muvio.common.Constants.THREE;
import static org.cyberrealm.tech.muvio.common.Constants.ZERO;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.cyberrealm.tech.muvio.dto.MediaBaseDto;
import org.cyberrealm.tech.muvio.dto.MediaDto;
import org.cyberrealm.tech.muvio.dto.MediaDtoWithCast;
import org.cyberrealm.tech.muvio.dto.MediaDtoWithCastFromDb;
import org.cyberrealm.tech.muvio.dto.MediaDtoWithPoints;
import org.cyberrealm.tech.muvio.dto.MediaGalleryRequestDto;
import org.cyberrealm.tech.muvio.dto.MediaVibeRequestDto;
import org.cyberrealm.tech.muvio.dto.PosterDto;
import org.cyberrealm.tech.muvio.dto.TitleDto;
import org.cyberrealm.tech.muvio.exception.EntityNotFoundException;
import org.cyberrealm.tech.muvio.mapper.MediaMapper;
import org.cyberrealm.tech.muvio.model.GenreEntity;
import org.cyberrealm.tech.muvio.model.Media;
import org.cyberrealm.tech.muvio.model.Type;
import org.cyberrealm.tech.muvio.repository.media.MediaRepository;
import org.cyberrealm.tech.muvio.service.MediaService;
import org.cyberrealm.tech.muvio.service.PaginationUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {
    private final MediaRepository mediaRepository;
    private final MediaMapper mediaMapper;
    private final PaginationUtil paginationUtil;

    @Override
    public MediaDto getMediaById(String id) {
        return mediaMapper.toMovieDto(mediaRepository.findMovieById(id)
                .orElseThrow(() -> new EntityNotFoundException("There is no media with this id: "
                        + id)));
    }

    @Override
    public Slice<MediaDtoWithPoints> getAllMediaByVibe(MediaVibeRequestDto requestDto,
                                                       Pageable pageable) {
        final List<MediaDtoWithPoints> mediasWithPoints = mediaRepository
                .getAllMediaByVibes(requestDto).stream()
                .map(media -> mediaMapper.toMediaDtoWithPoints(media,
                        getCategories(requestDto.categories())))
                .toList();
        return paginationUtil.paginateList(pageable, mediasWithPoints);
    }

    @Override
    public Slice<MediaBaseDto> getAllForGallery(MediaGalleryRequestDto requestDto,
                                                Pageable pageable) {
        final List<Media> mediaForGallery = mediaRepository.getAllForGallery(requestDto, pageable);
        final List<MediaBaseDto> listMedias = mediaForGallery.stream()
                .map(mediaMapper::toMediaBaseDto).toList();
        return new SliceImpl<>(listMedias, pageable, !listMedias.isEmpty());
    }

    @Override
    public Set<MediaDto> getAllLuck(int size) {
        return mediaRepository.getAllLuck(size).stream()
                .map(mediaMapper::toMovieDto).collect(Collectors.toSet());
    }

    @Transactional
    @Override
    public Slice<MediaBaseDto> getRecommendations(int page) {
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
        updateDuration(recommendations);
        final Page<MediaBaseDto> mediaPage = paginationUtil
                .paginateList(PageRequest.of(page, SIX), recommendations);
        return mediaPage.getContent().size() < SIX ? null : mediaPage;
    }

    @Override
    public Slice<MediaDtoWithCast> findMediaByTopLists(String topList, Pageable pageable) {
        final Slice<MediaDtoWithCastFromDb> media = mediaRepository
                .findByTopListsContaining(topList, pageable);
        final List<MediaDtoWithCast> mediaList = media.stream()
                .map(mediaMapper::toMediaDtoWithCast).toList();
        return new SliceImpl<>(mediaList, pageable, !mediaList.isEmpty());
    }

    @Override
    public Slice<PosterDto> findAllPosters(Pageable pageable) {
        return mediaRepository.findAllPosters(pageable);
    }

    @Override
    public Slice<TitleDto> findAllTitles(Pageable pageable) {
        return mediaRepository.findAllTitles(pageable);
    }

    @Override
    public MediaDto findByTitle(String title) {
        return mediaMapper.toMovieDto(mediaRepository.findByTitle(title));
    }

    @Override
    public List<MediaBaseDto> getAll(Pageable pageable) {
        return mediaRepository.getAll(pageable).stream().peek(media -> media.setDuration(
                mediaMapper.toDuration(Integer.valueOf(media.getDuration())))).toList();
    }

    @Override
    public long count() {
        return mediaRepository.count();
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
}
