package org.cyberrealm.tech.muvio.service;

import java.util.List;
import org.cyberrealm.tech.muvio.dto.MovieDto;
import org.cyberrealm.tech.muvio.model.Movie;
import org.springframework.data.domain.Pageable;

public interface MovieService {

    List<MovieDto> getAllMovies(Pageable pageable);

    Movie getMovieById(String id);

    Movie saveMovie(Movie movie);

    void deleteMovieById(String id);

    Movie updateMovie(String id, Movie updatedMovie);
}
