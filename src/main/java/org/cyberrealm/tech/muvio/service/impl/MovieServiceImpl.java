package org.cyberrealm.tech.muvio.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.cyberrealm.tech.muvio.model.Movie;
import org.cyberrealm.tech.muvio.repository.movies.MovieRepository;
import org.cyberrealm.tech.muvio.service.MovieService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {
    private final MovieRepository movieRepository;

    @Override
    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    @Override
    public Movie getMovieById(String id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("There is no movie with this id"));
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
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("There is no movie with this id"));
        movie.setName(updatedMovie.getName());
        movie.setGenres(updatedMovie.getGenres());
        movie.setRating(movie.getRating());
        movie.setTrailer(movie.getTrailer());
        return movieRepository.save(movie);
    }
}
