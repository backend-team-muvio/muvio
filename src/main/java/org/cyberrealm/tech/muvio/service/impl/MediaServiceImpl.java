package org.cyberrealm.tech.muvio.service.impl;

import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.cyberrealm.tech.muvio.dto.MediaDto;
import org.cyberrealm.tech.muvio.dto.MediaBaseDto;
import org.cyberrealm.tech.muvio.dto.MediaBaseDtoWithPoints;
import org.cyberrealm.tech.muvio.dto.MediaDtoWithCast;
import org.cyberrealm.tech.muvio.dto.MediaDtoWithCastFromDb;
import org.cyberrealm.tech.muvio.dto.MediaGalleryRequestDto;
import org.cyberrealm.tech.muvio.dto.MediaVibeRequestDto;
import org.cyberrealm.tech.muvio.dto.PosterDto;
import org.cyberrealm.tech.muvio.dto.TitleDto;
import org.cyberrealm.tech.muvio.exception.EntityNotFoundException;
import org.cyberrealm.tech.muvio.mapper.MediaMapper;
import org.cyberrealm.tech.muvio.model.Media;
import org.cyberrealm.tech.muvio.model.Type;
import org.cyberrealm.tech.muvio.repository.media.MediaRepository;
import org.cyberrealm.tech.muvio.service.MediaService;
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
    private static final int NINE = 9;
    private static final String DEFAULT_TITLE = "";
    private static final String SPLIT_PATTERN = "-";
    private static final int ZERO = 0;
    private static final int ONE = 1;
    private static final int TWO = 2;
    private static final int THREE = 3;
    private static final int DEFAULT_YEAR = 1900;
    private static final String RATING = "rating";
    private static final List<String> TOP_GENRES = List.of("ACTION", "DRAMA", "COMEDY");
    private final MediaRepository mediaRepository;
    private final MediaMapper mediaMapper;

    @Override
    public MediaDto getMediaById(String id) {
        return mediaMapper.toMovieDto(mediaRepository.findMovieById(id)
                .orElseThrow(() -> new EntityNotFoundException("There is no media with this id: "
                        + id)));
    }

    @Override
    public Media saveMedia(Media media) {
        return mediaRepository.save(media);
    }

    @Override
    public void deleteMediaById(String id) {
        mediaRepository.deleteById(id);
    }

    @Override
    public Media updateMedia(String id, Media updatedMedia) {
        final Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("There is no media with this id: "
                        + id));
        media.setTitle(updatedMedia.getTitle());
        media.setGenres(updatedMedia.getGenres());
        media.setRating(updatedMedia.getRating());
        media.setTrailer(updatedMedia.getTrailer());
        return mediaRepository.save(media);
    }

    @Override
    public Slice<MediaBaseDtoWithPoints> getAllMediaByVibe(MediaVibeRequestDto requestDto,
                                                           Pageable pageable) {
        final Integer[] years = getYearsIn(requestDto.years());
        final Set<String> categories;
        if (requestDto.categories() == null) {
            categories = Set.of();
        } else {
            categories = requestDto.categories();
        }
        final Slice<MediaBaseDtoWithPoints> mediaByVibes = mediaRepository.findAllByVibes(
                years[ZERO], years[ONE], getType(requestDto.type()),
                requestDto.vibe(), categories, pageable);
        updateDuration(mediaByVibes);
        return mediaByVibes;
    }

    @Override
    public Slice<MediaBaseDto> getAllForGallery(MediaGalleryRequestDto requestDto,
                                                Pageable pageable) {
        final Integer[] years = getYearsIn(requestDto.years());
        final String title;
        if (requestDto.title() != null) {
            title = requestDto.title();
        } else {
            title = DEFAULT_TITLE;
        }
        final Slice<MediaBaseDto> mediaForGallery = mediaRepository.getAllForGallery(years[ZERO],
                years[ONE], title, getType(requestDto.type()), pageable);
        updateDuration(mediaForGallery);
        return mediaForGallery;
    }

    @Override
    public Set<MediaBaseDto> getAllLuck(int size) {
        final Set<MediaBaseDto> luck = mediaRepository.getAllLuck(size);
        updateDuration(luck);
        return luck;
    }

    @Transactional
    @Override
    public Slice<MediaBaseDto> getRecommendations(Pageable pageable) {
        int minYear = Year.now().getValue() - THREE;
        final List<MediaBaseDto> recommendations = new ArrayList<>();
        TOP_GENRES.forEach(genre -> {
            addRecommendationsByTypeAndGenre(Type.MOVIE, genre, minYear, pageable,
                    recommendations);
            addRecommendationsByTypeAndGenre(Type.TV_SHOW, genre, minYear, pageable,
                    recommendations);
        });
        if (recommendations.size() != 6) {
            return null;
        }
        updateDuration(recommendations);
        return new SliceImpl<>(recommendations, pageable, !recommendations.isEmpty());
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

    private void addRecommendationsByTypeAndGenre(
            Type type, String genre, int minYear, Pageable pageable,
            List<MediaBaseDto> recommendations) {
        List<MediaBaseDto> mediaList = fetchMedia(type, genre, minYear, pageable);
        if (!mediaList.isEmpty() && recommendations.contains(mediaList.getFirst())) {
            mediaList = fetchMedia(type, genre, minYear, pageable.next());
        }
        recommendations.addAll(mediaList);
    }

    private List<MediaBaseDto> fetchMedia(Type type, String genre, int minYear,
                                          Pageable pageable) {
        Pageable sortedPageRequest = PageRequest.of(pageable.getPageNumber(),
                pageable.getPageSize(), Sort.by(RATING).descending());
        return mediaRepository.findMoviesByTypeGenreAndYears(type, genre, minYear,
                sortedPageRequest).getContent();
    }

    private Set<String> getType(String type) {
        if (type != null && !type.isBlank()) {
            return Set.of(type);
        }
        return Arrays.stream(Type.values()).map(Enum::toString).collect(Collectors.toSet());
    }

    private Integer[] getYearsIn(String years) {
        if (years != null && years.length() == NINE
                && years.contains(SPLIT_PATTERN)) {
            return Arrays.stream(years.split(SPLIT_PATTERN)).map(Integer::parseInt)
                    .toArray(Integer[]::new);
        } else {
            final Integer[] yearsDefault = new Integer[TWO];
            yearsDefault[ZERO] = DEFAULT_YEAR;
            yearsDefault[ONE] = Year.now().getValue();
            return yearsDefault;
        }
    }

    private <T extends MediaBaseDto> void updateDuration(Iterable<T> mediaList) {
        mediaList.forEach(media -> media.setDuration(
                mediaMapper.toDuration(Integer.parseInt(media.getDuration()))));
    }
}
