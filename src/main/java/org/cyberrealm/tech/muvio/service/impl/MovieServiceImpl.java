package org.cyberrealm.tech.muvio.service.impl;

import com.uwetrottmann.tmdb2.Tmdb;
import com.uwetrottmann.tmdb2.entities.BaseMovie;
import com.uwetrottmann.tmdb2.entities.Genre;
import com.uwetrottmann.tmdb2.entities.GenreResults;
import com.uwetrottmann.tmdb2.entities.MovieResultsPage;
import com.uwetrottmann.tmdb2.entities.Videos;
import com.uwetrottmann.tmdb2.services.GenresService;
import com.uwetrottmann.tmdb2.services.MoviesService;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.cyberrealm.tech.muvio.model.GenreEntity;
import org.cyberrealm.tech.muvio.model.Movie;
import org.cyberrealm.tech.muvio.repository.genres.GenreRepository;
import org.cyberrealm.tech.muvio.repository.movies.MovieRepository;
import org.cyberrealm.tech.muvio.service.MovieService;
import org.springframework.stereotype.Service;
import retrofit2.Response;

@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {
    private static final String UA = "ua";
    private static final String IMAGE_PATH = "https://image.tmdb.org/t/p/w500";
    private static final String YOUTUBE_PATH = "https://www.youtube.com/watch?v=";
    private static final int ZERO = 0;
    private static final int ONE = 1;
    private static final int FIVE = 5;
    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final Tmdb tmdb;

    @PostConstruct
    @Override
    public void importMovies() {
        if (movieRepository != null) {
            movieRepository.deleteAll();
        }
        loadGenres();
        MoviesService moviesService = tmdb.moviesService();
        for (int page = ONE; page <= FIVE; page++) {
            Response<MovieResultsPage> response;
            try {
                response = moviesService.popular(page, UA, UA).execute();
            } catch (IOException e) {
                throw new RuntimeException("Failed to fetch popular movies");
            }
            MovieResultsPage movieResultsPage = response.body();
            if (movieResultsPage != null) {
                List<BaseMovie> movies = movieResultsPage.results;
                if (movies != null) {
                    for (BaseMovie movie : movies) {
                        Movie movieDb = new Movie();
                        movieDb.setName(movie.title);
                        movieDb.setPosterPath(IMAGE_PATH + movie.poster_path);
                        if (movie.genre_ids != null && !movie.genre_ids.isEmpty()) {
                            Set<GenreEntity> genres = movie.genre_ids.stream()
                                    .map(genreId -> genreRepository.findById(
                                            String.valueOf(genreId)).orElseThrow(
                                                    () -> new RuntimeException(
                                                            "Can't find genre by id " + genreId)))
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.toSet());
                            movieDb.setGenres(genres);
                        }
                        if (movie.id != null) {
                            Response<Videos> videosResponse;
                            try {
                                videosResponse = moviesService.videos(movie.id, UA).execute();
                            } catch (IOException e) {
                                throw new RuntimeException("Can not find videos");
                            }
                            List<Videos.Video> trailers = Objects.requireNonNull(
                                    Objects.requireNonNull(videosResponse.body()).results)
                                    .stream().filter(video -> {
                                        if (video.type != null) {
                                            return "Trailer".equals(video.type.toString());
                                        }
                                        return false;
                                    })
                                    .toList();
                            if (!trailers.isEmpty()) {
                                movieDb.setTrailer(YOUTUBE_PATH + trailers.get(ZERO).key);
                            }
                        }
                        if (movie.vote_average != null) {
                            movieDb.setRating(movie.vote_average);
                        }
                        Objects.requireNonNull(movieRepository).save(movieDb);
                    }
                }
            }
        }
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
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("There is no movie with this id: " + id));
        movie.setName(updatedMovie.getName());
        movie.setGenres(updatedMovie.getGenres());
        movie.setRating(updatedMovie.getRating());
        movie.setTrailer(updatedMovie.getTrailer());
        movie.setPosterPath(IMAGE_PATH + updatedMovie.getPosterPath());
        return movieRepository.save(movie);
    }

    private void loadGenres() {
        if (genreRepository != null) {
            genreRepository.deleteAll();
        }
        GenresService genresService = tmdb.genreService();
        Response<GenreResults> response;
        try {
            response = genresService.movie(UA).execute();
        } catch (IOException e) {
            throw new RuntimeException("Failed execute genres");
        }
        if (response.body() != null) {
            List<Genre> genreEntities = response.body().genres;
            for (Genre genre : Objects.requireNonNull(genreEntities)) {
                GenreEntity genreEntity = new GenreEntity();
                genreEntity.setId(String.valueOf(genre.id));
                genreEntity.setName(genre.name);
                Objects.requireNonNull(genreRepository).save(genreEntity);
            }
        }
    }
}
