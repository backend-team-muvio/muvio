package org.cyberrealm.tech.muvio.service.impl;

import com.uwetrottmann.tmdb2.Tmdb;
import com.uwetrottmann.tmdb2.entities.BaseMovie;
import com.uwetrottmann.tmdb2.entities.GenreResults;
import com.uwetrottmann.tmdb2.entities.MovieResultsPage;
import com.uwetrottmann.tmdb2.entities.Videos;
import com.uwetrottmann.tmdb2.services.MoviesService;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cyberrealm.tech.muvio.model.GenreEntity;
import org.cyberrealm.tech.muvio.model.Movie;
import org.cyberrealm.tech.muvio.repository.genres.GenreRepository;
import org.cyberrealm.tech.muvio.repository.movies.MovieRepository;
import org.cyberrealm.tech.muvio.service.MovieService;
import org.springframework.stereotype.Service;
import retrofit2.Response;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieServiceImpl implements MovieService {
    private static final String UA = "ua";
    private static final String IMAGE_PATH = "https://image.tmdb.org/t/p/w500";
    private static final String YOUTUBE_PATH = "https://www.youtube.com/watch?v=";
    private static final int PAGES_TO_LOAD = 5;

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final Tmdb tmdb;

    @PostConstruct
    @Override
    public void importMovies() {
        movieRepository.deleteAll();
        loadGenres();
        fetchAndSaveMovies();
    }

    @Override
    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
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
        Movie movie = getMovieById(id);
        movie.setName(updatedMovie.getName());
        movie.setGenres(updatedMovie.getGenres());
        movie.setRating(updatedMovie.getRating());
        movie.setTrailer(updatedMovie.getTrailer());
        movie.setPosterPath(IMAGE_PATH + updatedMovie.getPosterPath());
        return movieRepository.save(movie);
    }

    private void loadGenres() {
        genreRepository.deleteAll();
        try {
            Response<GenreResults> response = tmdb.genreService().movie(UA).execute();
            if (response.isSuccessful() && response.body() != null) {
                List<GenreEntity> genres = response.body().genres.stream()
                        .map(genre -> {
                            GenreEntity genreEntity = new GenreEntity();
                            genreEntity.setId(String.valueOf(genre.id));
                            genreEntity.setName(genre.name);
                            return genreEntity;
                        })
                        .toList();
                genreRepository.saveAll(genres);
            }
        } catch (IOException e) {
            log.error("Failed to fetch genres", e);
            throw new RuntimeException("Failed to fetch genres", e);
        }
    }

    private void fetchAndSaveMovies() {
        MoviesService moviesService = tmdb.moviesService();
        for (int page = 1; page <= PAGES_TO_LOAD; page++) {
            try {
                Response<MovieResultsPage> response = moviesService.popular(page, UA, UA).execute();
                if (response.isSuccessful() && response.body() != null) {
                    saveMovies(response.body().results);
                }
            } catch (IOException e) {
                log.error("Failed to fetch movies for page {}", page, e);
            }
        }
    }

    private void saveMovies(List<BaseMovie> movies) {
        for (BaseMovie baseMovie : movies) {
            Movie movie = new Movie();
            movie.setName(baseMovie.title);
            movie.setPosterPath(IMAGE_PATH + baseMovie.poster_path);
            movie.setGenres(mapGenres(baseMovie.genre_ids));
            movie.setTrailer(fetchTrailer(baseMovie.id));
            movie.setRating(baseMovie.vote_average);
            movieRepository.save(movie);
        }
    }

    private Set<GenreEntity> mapGenres(List<Integer> genreIds) {
        return genreIds.stream()
                .map(id -> genreRepository.findById(String.valueOf(id))
                        .orElseThrow(() -> new RuntimeException("Can't find genre by id " + id)))
                .collect(Collectors.toSet());
    }

    private String fetchTrailer(Integer movieId) {
        if (movieId == null) {
            return null;
        }
        try {
            Response<Videos> response = tmdb.moviesService().videos(movieId, UA).execute();
            if (response.isSuccessful() && response.body() != null) {
                return response.body().results.stream()
                        .filter(video -> "Trailer".equals(video.type))
                        .map(video -> YOUTUBE_PATH + video.key)
                        .findFirst()
                        .orElse(null);
            }
        } catch (IOException e) {
            log.error("Failed to fetch trailer for movie id {}", movieId, e);
        }
        return null;
    }
}
