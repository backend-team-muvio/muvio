package org.cyberrealm.tech.muvio.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.model.core.Genre;
import info.movito.themoviedbapi.model.core.MovieResultsPage;
import info.movito.themoviedbapi.model.core.NamedIdElement;
import info.movito.themoviedbapi.model.movies.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.cyberrealm.tech.muvio.dto.MovieDto;
import org.cyberrealm.tech.muvio.mapper.MovieMapper;
import org.cyberrealm.tech.muvio.model.*;
import org.cyberrealm.tech.muvio.repository.actors.ActorRepository;
import org.cyberrealm.tech.muvio.repository.movies.MovieRepository;
import org.cyberrealm.tech.muvio.service.CategoryService;
import org.cyberrealm.tech.muvio.service.MovieService;
import org.cyberrealm.tech.muvio.service.TmdbService;
import org.cyberrealm.tech.muvio.service.VibeService;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService, SmartLifecycle {
    private static final int ZERO = 0;
    private static final int ONE = 1;
    private static final int LAST_PAGE = 5;
    private static final String REGION = "US";
    private static final String LANGUAGE = "en";
    private static final String IMAGE_PATH = "https://image.tmdb.org/t/p/w500";
    private static final String DIRECTOR = "Director";
    private final MovieRepository movieRepository;
    private final MovieMapper movieMapper;
    private final TmdbService tmdbService;
    private final CategoryService categoryService;
    private final TmdbApi tmdbApi;
    private final ActorRepository actorRepository;
    private final VibeService vibeService;
    private boolean isRunning;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void importMovies(int fromPage, int toPage, String language, String location) {
        tmdbService.deleteAll();
        final Set<String> imdbTop250 = categoryService.getImdbTop250();
        final TmdbMovies tmdbMovies = tmdbApi.getMovies();
        int count = 1;
        final Set<Movie> moviesSet = new HashSet<>();
        List<CompletableFuture<Set<Movie>>> futures = n
        for (int page = fromPage; page <= toPage; page++) {
            final MovieResultsPage movieResultsPage = tmdbService.getMovieResultsPage(language, page, location);
            final List<info.movito.themoviedbapi.model.core.Movie> movies = movieResultsPage.getResults();
            for (info.movito.themoviedbapi.model.core.Movie movie : movies) {
                final int movieId = movie.getId();
                final MovieDb movieDb = tmdbService.getMovieDb(movieId, language);
                final KeywordResults keywords = tmdbService.getKeywordResults(tmdbMovies, movieId);
                final Credits credits = tmdbService.getCredits(tmdbMovies, movieId, language);
                final List<ReleaseInfo> releaseInfo = tmdbService.getReleaseInfo(tmdbMovies, movieId);
                final org.cyberrealm.tech.muvio.model.Movie movieMdb =
                        new org.cyberrealm.tech.muvio.model.Movie();
                movieMdb.setPhotos(tmdbService.getPhotos(tmdbMovies, movieId, language));
                movieMdb.setTrailer(tmdbService.getTrailer(tmdbMovies, movieId, language));
                movieMdb.setId(String.valueOf(movieId));
                final String title = movieDb.getTitle();
                movieMdb.setName(title);
                movieMdb.setPosterPath(IMAGE_PATH + movieDb.getPosterPath());
                movieMdb.setReleaseYear(getReleaseYear(movieDb.getReleaseDate()));
                final String overview = movieDb.getOverview();
                movieMdb.setOverview(overview);
                final Double rating = movieDb.getVoteAverage();
                movieMdb.setRating(rating);
                movieMdb.setDuration(movieDb.getRuntime());
                movieMdb.setDirector(getDirector(credits.getCrew()));
                movieMdb.setActors(getActors(credits.getCast()));
                final Set<GenreEn> genres = getGenres(movieDb.getGenres());
                movieMdb.setGenres(genres);
                movieMdb.setVibes(vibeService.getVibes(releaseInfo, genres));
                movieMdb.setCategories(categoryService.getCategories(overview.toLowerCase(),
                        keywords, rating, movieDb.getVoteCount(),
                        movieDb.getPopularity(), imdbTop250, title));
                movieMdb.setType(Type.MOVIE);
                movieMdb.setReviewDbs(tmdbService.getReviews(tmdbMovies, movieId, language));
                moviesSet.add(movieMdb);
                System.out.println("Count: " + count++);
            }
        }
        movieRepository.saveAll(moviesSet);
    }

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
        importMovies(ZERO, LAST_PAGE, LANGUAGE, REGION);
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

    private Map<String, Actor> getActors(List<Cast> casts) {
        final Set<String> actorNames = casts.stream()
                .map(Cast::getName)
                .collect(Collectors.toSet());
        final Map<String, Actor> existingActors = actorRepository.findAllById(actorNames)
                .stream()
                .collect(Collectors.toMap(Actor::getName, actor -> actor));
        return casts.stream().collect(Collectors.toMap(cast -> cast.getCharacter().replace(".", "_"),
                        cast -> existingActors.computeIfAbsent(cast.getName(), name -> {
                            Actor newActor = new Actor();
                            newActor.setName(name);
                            newActor.setPhoto(IMAGE_PATH + cast.getProfilePath());
                            return actorRepository.save(newActor);
                        }),
                        (existing, replacement) -> existing
                ));
    }

    private String getDirector(List<Crew> crews) {
        return crews.stream().filter(crew -> {
                    final String job = crew.getJob();
                    return job.equals(DIRECTOR);
                }).findFirst()
                .map(NamedIdElement::getName).orElse("Unknown");
    }

    private Set<GenreEn> getGenres(List<Genre> genres) {
        return genres.stream()
                .map(genre -> GenreEn.fromString(genre.getName()))
                .collect(Collectors.toSet());
    }

    private Integer getReleaseYear(String releaseDate) {
        if (releaseDate != null && releaseDate.length() == 10) {
            return Integer.parseInt(releaseDate.substring(0, 4));
        }
        return 0;
    }
}
