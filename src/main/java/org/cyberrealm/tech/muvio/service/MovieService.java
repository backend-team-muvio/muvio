package org.cyberrealm.tech.muvio.service;

import java.util.Set;
import org.cyberrealm.tech.muvio.dto.MovieBaseDto;
import org.cyberrealm.tech.muvio.dto.MovieBaseDtoWithPoints;
import org.cyberrealm.tech.muvio.dto.MovieDto;
import org.cyberrealm.tech.muvio.dto.MovieDtoWithCast;
import org.cyberrealm.tech.muvio.dto.MovieGalleryRequestDto;
import org.cyberrealm.tech.muvio.dto.MovieVibeRequestDto;
import org.cyberrealm.tech.muvio.dto.PosterDto;
import org.cyberrealm.tech.muvio.dto.TitleDto;
import org.cyberrealm.tech.muvio.model.Movie;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface MovieService {

    MovieDto getMovieById(String id);

    Movie saveMovie(Movie movie);

    void deleteMovieById(String id);

    Movie updateMovie(String id, Movie updatedMovie);

    Slice<MovieBaseDtoWithPoints> getAllMoviesByVibe(MovieVibeRequestDto requestDto,
                                                     Pageable pageable);

    Slice<MovieBaseDto> getAllForGallery(MovieGalleryRequestDto requestDto, Pageable pageable);

    Set<MovieBaseDto> getAllLuck(int size);

    Slice<MovieBaseDto> getRecommendations(Pageable pageable);

    Slice<MovieDtoWithCast> findMoviesByTopLists(String topList, Pageable pageable);

    Slice<PosterDto> findAllPosters(Pageable pageable);

    Slice<TitleDto> findAllTitles(Pageable pageable);

    MovieDto findByTitle(String title);
}
