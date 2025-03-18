package org.cyberrealm.tech.muvio.service;

import java.util.Set;
import org.cyberrealm.tech.muvio.dto.MediaDto;
import org.cyberrealm.tech.muvio.dto.MovieBaseDto;
import org.cyberrealm.tech.muvio.dto.MovieBaseDtoWithPoints;
import org.cyberrealm.tech.muvio.dto.MovieDtoWithCast;
import org.cyberrealm.tech.muvio.dto.MovieGalleryRequestDto;
import org.cyberrealm.tech.muvio.dto.MovieVibeRequestDto;
import org.cyberrealm.tech.muvio.dto.PosterDto;
import org.cyberrealm.tech.muvio.dto.TitleDto;
import org.cyberrealm.tech.muvio.model.Media;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface MediaService {

    MediaDto getMovieById(String id);

    Media saveMovie(Media media);

    void deleteMovieById(String id);

    Media updateMovie(String id, Media updatedMedia);

    Slice<MovieBaseDtoWithPoints> getAllMoviesByVibe(MovieVibeRequestDto requestDto,
                                                     Pageable pageable);

    Slice<MovieBaseDto> getAllForGallery(MovieGalleryRequestDto requestDto, Pageable pageable);

    Set<MovieBaseDto> getAllLuck(int size);

    Slice<MovieBaseDto> getRecommendations(Pageable pageable);

    Slice<MovieDtoWithCast> findMoviesByTopLists(String topList, Pageable pageable);

    Slice<PosterDto> findAllPosters(Pageable pageable);

    Slice<TitleDto> findAllTitles(Pageable pageable);

    MediaDto findByTitle(String title);
}
