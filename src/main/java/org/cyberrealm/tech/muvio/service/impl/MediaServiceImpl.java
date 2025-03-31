package org.cyberrealm.tech.muvio.service.impl;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
import org.cyberrealm.tech.muvio.model.Media;
import org.cyberrealm.tech.muvio.model.Type;
import org.cyberrealm.tech.muvio.repository.media.MediaRepository;
import org.cyberrealm.tech.muvio.service.MediaService;
import org.cyberrealm.tech.muvio.service.PaginationUtil;
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
    private static final int THREE = 3;
    private static final int SIX = 6;
    private static final String RATING = "rating";
    private static final List<String> TOP_GENRES = List.of("CRIME", "DRAMA", "COMEDY");
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
    public Slice<MediaDtoWithPoints> getAllMediaByVibe(MediaVibeRequestDto requestDto,
                                                       Pageable pageable) {
        final List<MediaDtoWithPoints> mediasWithPoints = mediaRepository
                .getAllMediaByVibes(requestDto).stream().map(media ->
                        mediaMapper.toMediaDtoWithPoints(media, requestDto.categories())).toList();
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
    public Slice<MediaBaseDto> getRecommendations(Pageable pageable) {
        int minYear = Year.now().getValue() - THREE;
        final List<MediaBaseDto> recommendations = new ArrayList<>();
        TOP_GENRES.forEach(genre -> {
            addRecommendationsByTypeAndGenre(Type.MOVIE, genre, minYear, pageable,
                    recommendations);
            addRecommendationsByTypeAndGenre(Type.TV_SHOW, genre, minYear, pageable,
                    recommendations);
        });
        if (recommendations.size() != SIX) {
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

    private <T extends MediaBaseDto> void updateDuration(Iterable<T> mediaList) {
        mediaList.forEach(media -> {
            if (media.getDuration() != null) {
                media.setDuration(
                        mediaMapper.toDuration(Integer.parseInt(media.getDuration())));
            }
        });
    }
}
