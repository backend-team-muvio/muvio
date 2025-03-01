package org.cyberrealm.tech.muvio.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.cyberrealm.tech.muvio.dto.MovieDto;
import org.cyberrealm.tech.muvio.mapper.MovieMapper;
import org.cyberrealm.tech.muvio.model.Movie;
import org.cyberrealm.tech.muvio.repository.movies.MovieRepository;
import org.cyberrealm.tech.muvio.service.MovieService;
import org.cyberrealm.tech.muvio.service.TmdbService;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService, SmartLifecycle {
    private static final int ZERO = 0;
    private static final int ONE = 1;
    private static final int LAST_PAGE = 164;
    private static final String REGION = "US";
    private static final String LANGUAGE = "en";
    private final MovieRepository movieRepository;
    private final MovieMapper movieMapper;
    private final TmdbService tmdbService;
    private boolean isRunning;

    @Override
    public List<MovieDto> getAllMovies() {
        return movieRepository.findAll().stream().map(movieMapper::toMovieDto).toList();
    }

    @Override
    public Movie getMovieById(String id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("There is no movie with this id: " + id));
    }

    @Override
    public Movie saveMovie(Movie movie) {
        return movieRepository.save(movie);
    }

    @Override
    public void deleteMovieById(String id) {
        movieRepository.deleteById(id);
    }

    @Override
    public Movie updateMovie(String id, Movie updatedMovie) {
        final Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("There is no movie with this id: " + id));
        movie.setName(updatedMovie.getName());
        movie.setGenres(updatedMovie.getGenres());
        movie.setRating(updatedMovie.getRating());
        movie.setTrailer(updatedMovie.getTrailer());
        return movieRepository.save(movie);
    }

    @Override
    public void start() {
        tmdbService.importMovies(ZERO, LAST_PAGE, LANGUAGE, REGION);
        isRunning = true;
    }

    @Override
    public void stop() {
        isRunning = false;
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public int getPhase() {
        return ONE;
    }
}
