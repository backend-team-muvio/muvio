package org.cyberrealm.tech.muvio.service.impl;

import static org.cyberrealm.tech.muvio.service.impl.TmdbServiceImpl.IMAGE_PATH;

import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.model.core.Genre;
import info.movito.themoviedbapi.model.movies.Cast;
import info.movito.themoviedbapi.model.movies.Credits;
import info.movito.themoviedbapi.model.movies.Crew;
import info.movito.themoviedbapi.model.movies.KeywordResults;
import info.movito.themoviedbapi.model.movies.MovieDb;
import info.movito.themoviedbapi.model.movies.ReleaseInfo;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.cyberrealm.tech.muvio.exception.MovieSyncException;
import org.cyberrealm.tech.muvio.exception.TmdbServiceException;
import org.cyberrealm.tech.muvio.mapper.ActorMapper;
import org.cyberrealm.tech.muvio.mapper.GenreMapper;
import org.cyberrealm.tech.muvio.mapper.MovieMapper;
import org.cyberrealm.tech.muvio.mapper.ReviewMapper;
import org.cyberrealm.tech.muvio.model.Actor;
import org.cyberrealm.tech.muvio.model.GenreEntity;
import org.cyberrealm.tech.muvio.model.Movie;
import org.cyberrealm.tech.muvio.model.Review;
import org.cyberrealm.tech.muvio.repository.actors.ActorRepository;
import org.cyberrealm.tech.muvio.repository.genres.GenreRepository;
import org.cyberrealm.tech.muvio.repository.movies.MovieRepository;
import org.cyberrealm.tech.muvio.service.CategoryService;
import org.cyberrealm.tech.muvio.service.MovieSyncService;
import org.cyberrealm.tech.muvio.service.TmdbService;
import org.cyberrealm.tech.muvio.service.VibeService;
import org.springframework.context.SmartLifecycle;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MovieSyncServiceImpl implements MovieSyncService, SmartLifecycle {
    private static final int ZERO = 0;
    private static final int ONE = 1;
    private static final int LAST_PAGE = 164;
    private static final String REGION = "US";
    private static final String LANGUAGE = "en";
    private static final String PRODUCER = "Producer";
    private static final String DIRECTOR = "Director";
    private static final int MAX_ATTEMPTS = 60;
    private static final int BACK_OFF = 1000;
    private static final String DEFAULT_PRODUCER = "Unknown";
    private boolean isRunning;
    private final TmdbService tmdbService;
    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final ActorRepository actorRepository;
    private final CategoryService categoryService;
    private final VibeService vibeService;
    private final GenreMapper genreMapper;
    private final MovieMapper movieMapper;
    private final ActorMapper actorMapper;
    private final ReviewMapper reviewMapper;

    @Override
    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    public void loadGenres(String language) {
        final List<GenreEntity> genres = tmdbService.fetchGenres(language).stream()
                .map(genreMapper::toGenreEntity)
                .toList();
        genreRepository.saveAll(genres);
    }

    @Transactional(rollbackFor = Exception.class)
    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public void importMovies(int fromPage, int toPage, String language, String location) {
        deleteAll();
        loadGenres(language);
        final Set<String> imdbTop250 = categoryService.getImdbTop250();
        final TmdbMovies tmdbMovies = tmdbService.getTmdbMovies();
        final List<info.movito.themoviedbapi.model.core.Movie> movieList =
                tmdbService.fetchPopularMovies(fromPage, toPage, language, location);
        final List<Movie> movies = movieList.stream()
                .map(movieTmdb -> createMovie(language, movieTmdb, tmdbMovies, imdbTop250))
                .toList();
        movieRepository.saveAll(movies);
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

    @Override
    public void deleteAll() {
        if (actorRepository != null) {
            actorRepository.deleteAll();
        }
        if (movieRepository != null) {
            movieRepository.deleteAll();
        }
        if (genreRepository != null) {
            genreRepository.deleteAll();
        }
    }

    private Movie createMovie(String language,
                              info.movito.themoviedbapi.model.core.Movie movieTmdb,
                              TmdbMovies tmdbMovies, Set<String> imdbTop250) {
        final MovieDb movieDb;
        final int movieId = movieTmdb.getId();
        final KeywordResults keywords;
        final Credits credits;
        final List<ReleaseInfo> releaseInfo;

        movieDb = tmdbService.fetchMovieDetails(tmdbMovies, movieId, language);
        final Movie movie = movieMapper.toEntity(movieDb);
        credits = tmdbService.fetchMovieCredits(tmdbMovies, movieId, language);
        keywords = tmdbService.fetchKeywords(tmdbMovies, movieId);
        releaseInfo = tmdbService.fetchReleaseInfo(tmdbMovies, movieId);

        movie.setPosterPath(IMAGE_PATH + movieDb.getPosterPath());
        movie.setTrailer(tmdbService.fetchTrailer(tmdbMovies, movieId, language));
        movie.setPhotos(tmdbService.fetchPhotos(tmdbMovies, language, movieId));
        movie.setReleaseYear(getReleaseYear(movieDb.getReleaseDate()));
        movie.setProducer(getProducer(credits.getCrew()));
        Set<Actor> actors = getActors(credits.getCast());
        actorRepository.saveAll(actors);
        movie.setActors(actors);
        final Set<GenreEntity> genres = getGenres(movieDb.getGenres());
        movie.setGenres(genres);
        movie.setReviews(getReviews(tmdbMovies, language, movieId));
        movie.setVibes(vibeService.getVibes(releaseInfo, genres));
        movie.setCategories(categoryService.getCategories(movie.getOverview().toLowerCase(),
                keywords, movieDb.getVoteAverage(), movieDb.getVoteCount(),
                movieDb.getPopularity(), imdbTop250, movie.getTitle()));
        return movie;
    }

    private List<Review> getReviews(TmdbMovies tmdbMovies, String language, int movieId) {
        return tmdbService.fetchMovieReviews(tmdbMovies, language, movieId).stream()
                .map(reviewMapper::toEntity)
                .toList();
    }

    private Set<Actor> getActors(List<Cast> casts) {
        return casts.stream().map(cast -> {
            final String name = cast.getName();
            return actorRepository.findById(name)
                .orElseGet(() -> {
                    final Actor actor = actorMapper.toActorEntity(cast);
                    actor.setPhoto(IMAGE_PATH + cast.getProfilePath());
                    return actor;
                });
        }).collect(Collectors.toSet());
    }

    private String getProducer(List<Crew> crews) {
        return crews.stream().filter(crew -> {
            final String job = crew.getJob();
            if (job.equals(DIRECTOR)) {
                return true;
            }
            return job.equals(PRODUCER);
        }).findFirst()
                .map(Crew::getOriginalName)
                .orElse(DEFAULT_PRODUCER);
    }

    private Set<GenreEntity> getGenres(List<Genre> genres) {
        return genres.stream()
                .map(genre -> genreRepository.findById(genre.getId())
                    .orElseThrow(() -> new MovieSyncException(
                        "Can't find genre by id " + genre.getId())))
                .collect(Collectors.toSet());
    }

    private Integer getReleaseYear(String releaseDate) {
        if (releaseDate != null && releaseDate.length() == 10) {
            return Integer.parseInt(releaseDate.substring(0, 4));
        }
        return 0;
    }
}
