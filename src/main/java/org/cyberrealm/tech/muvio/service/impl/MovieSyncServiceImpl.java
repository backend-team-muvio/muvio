package org.cyberrealm.tech.muvio.service.impl;

import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.model.core.Genre;
import info.movito.themoviedbapi.model.movies.Cast;
import info.movito.themoviedbapi.model.movies.Credits;
import info.movito.themoviedbapi.model.movies.Crew;
import info.movito.themoviedbapi.model.movies.KeywordResults;
import info.movito.themoviedbapi.model.movies.MovieDb;
import info.movito.themoviedbapi.model.movies.ReleaseInfo;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.cyberrealm.tech.muvio.exception.EntityNotFoundException;
import org.cyberrealm.tech.muvio.mapper.ActorMapper;
import org.cyberrealm.tech.muvio.mapper.GenreMapper;
import org.cyberrealm.tech.muvio.mapper.MovieMapper;
import org.cyberrealm.tech.muvio.mapper.ReviewMapper;
import org.cyberrealm.tech.muvio.model.Actor;
import org.cyberrealm.tech.muvio.model.GenreEntity;
import org.cyberrealm.tech.muvio.model.Movie;
import org.cyberrealm.tech.muvio.model.Review;
import org.cyberrealm.tech.muvio.model.Type;
import org.cyberrealm.tech.muvio.repository.actors.ActorRepository;
import org.cyberrealm.tech.muvio.repository.movies.MovieRepository;
import org.cyberrealm.tech.muvio.service.CategoryService;
import org.cyberrealm.tech.muvio.service.MovieSyncService;
import org.cyberrealm.tech.muvio.service.TmdbService;
import org.cyberrealm.tech.muvio.service.VibeService;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MovieSyncServiceImpl implements MovieSyncService, SmartLifecycle {
    public static final String IMAGE_PATH = "https://image.tmdb.org/t/p/w500";
    private static final int BATCH_SIZE = 100;
    private static final int ZERO = 0;
    private static final int ONE = 1;
    private static final int LAST_PAGE = 5;
    private static final String REGION = "US";
    private static final String LANGUAGE = "en";
    private static final String DIRECTOR = "Director";
    private static final String DEFAULT_PRODUCER = "Unknown";
    private boolean isRunning;
    private final TmdbService tmdbService;
    private final MovieRepository movieRepository;
    private final ActorRepository actorRepository;
    private final CategoryService categoryService;
    private final VibeService vibeService;
    private final GenreMapper genreMapper;
    private final MovieMapper movieMapper;
    private final ActorMapper actorMapper;
    private final ReviewMapper reviewMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void importMovies(int fromPage, int toPage, String language, String location) {
        deleteAll();
        final Set<String> imdbTop250 = categoryService.getImdbTop250();
        final TmdbMovies tmdbMovies = tmdbService.getTmdbMovies();
        final List<info.movito.themoviedbapi.model.core.Movie> movieList =
                tmdbService.fetchPopularMovies(fromPage, toPage, language, location);
        final List<Movie> movies;
        try (ForkJoinPool pool = new ForkJoinPool(TmdbServiceImpl.LIMIT_THREADS)) {
            try {
                movies = pool.submit(() -> movieList.parallelStream()
                        .map(movieTmdb -> createMovie(language, movieTmdb, tmdbMovies, imdbTop250))
                        .toList()).get();
            } catch (InterruptedException | ExecutionException e) {
                throw new EntityNotFoundException("Failed to process movies with thread pool");
            }
        }
        for (int i = 0; i < movies.size(); i += BATCH_SIZE) {
            int toIndex = Math.min(i + BATCH_SIZE, movies.size());
            movieRepository.saveAll(movies.subList(i, toIndex));
        }
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
        movie.setDirector(getDirector(credits.getCrew()));
        movie.setActors(getActors(credits.getCast()));
        final Set<GenreEntity> genres = getGenres(movieDb.getGenres());
        movie.setGenres(genres);
        movie.setReviews(getReviews(tmdbMovies, language, movieId));
        movie.setVibes(vibeService.getVibes(releaseInfo, genres));
        movie.setCategories(categoryService.getCategories(movie.getOverview().toLowerCase(),
                keywords, movieDb.getVoteAverage(), movieDb.getVoteCount(),
                movieDb.getPopularity(), imdbTop250, movie.getTitle()));
        movie.setType(Type.MOVIE);
        return movie;
    }

    private List<Review> getReviews(TmdbMovies tmdbMovies, String language, int movieId) {
        return tmdbService.fetchMovieReviews(tmdbMovies, language, movieId).stream()
                .map(reviewMapper::toEntity)
                .toList();
    }

    private Map<String, Actor> getActors(List<Cast> casts) {
        final Set<Actor> newActors = new HashSet<>();
        final Map<String, Actor> actorsMap = casts.stream().collect(Collectors.toMap(cast ->
                        cast.getCharacter().replace(".", "_"), cast -> {
                final String name = cast.getName();
                return actorRepository.findById(name).orElseGet(() -> {
                    final Actor actor = actorMapper.toActorEntity(cast);
                    actor.setPhoto(IMAGE_PATH + cast.getProfilePath());
                    newActors.add(actor);
                    return actor;
                });
            },
                (existingActor, duplicateActor) -> existingActor));
        if (!newActors.isEmpty()) {
            actorRepository.saveAll(newActors);
        }
        return actorsMap;
    }

    private String getDirector(List<Crew> crews) {
        return crews.stream().filter(crew -> crew.getJob().equals(DIRECTOR))
                .findFirst()
                .map(Crew::getOriginalName)
                .orElse(DEFAULT_PRODUCER);
    }

    private Set<GenreEntity> getGenres(List<Genre> genres) {
        return genres.stream()
                .map(genreMapper::toGenreEntity).collect(Collectors.toSet());
    }

    private Integer getReleaseYear(String releaseDate) {
        if (releaseDate != null && releaseDate.length() == 10) {
            return Integer.parseInt(releaseDate.substring(0, 4));
        }
        return 0;
    }
}
