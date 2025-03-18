package org.cyberrealm.tech.muvio.service.impl;

import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.model.core.Genre;
import info.movito.themoviedbapi.model.core.Movie;
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
import org.cyberrealm.tech.muvio.exception.MovieProcessingException;
import org.cyberrealm.tech.muvio.mapper.ActorMapper;
import org.cyberrealm.tech.muvio.mapper.GenreMapper;
import org.cyberrealm.tech.muvio.mapper.MediaMapper;
import org.cyberrealm.tech.muvio.mapper.ReviewMapper;
import org.cyberrealm.tech.muvio.model.Actor;
import org.cyberrealm.tech.muvio.model.GenreEntity;
import org.cyberrealm.tech.muvio.model.Media;
import org.cyberrealm.tech.muvio.model.Review;
import org.cyberrealm.tech.muvio.model.Type;
import org.cyberrealm.tech.muvio.repository.actors.ActorRepository;
import org.cyberrealm.tech.muvio.repository.media.MediaRepository;
import org.cyberrealm.tech.muvio.service.CategoryService;
import org.cyberrealm.tech.muvio.service.MediaSyncService;
import org.cyberrealm.tech.muvio.service.TmdbService;
import org.cyberrealm.tech.muvio.service.TopListService;
import org.cyberrealm.tech.muvio.service.VibeService;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MediaSyncServiceImpl implements MediaSyncService, SmartLifecycle {
    private static final int SHORT_DURATION = 40;
    private static final String IMAGE_PATH = "https://image.tmdb.org/t/p/w500";
    private static final int LIMIT_THREADS =
            Math.min(10, Runtime.getRuntime().availableProcessors() * 2);
    private static final int BATCH_SIZE = 100;
    private static final int ZERO = 0;
    private static final int ONE = 1;
    private static final int LAST_PAGE = 10;
    private static final String REGION = "US";
    private static final String LANGUAGE = "en";
    private static final String DIRECTOR = "Director";
    private static final String PRODUCER = "Producer";
    private boolean isRunning;
    private final TmdbService tmdbService;
    private final MediaRepository mediaRepository;
    private final ActorRepository actorRepository;
    private final CategoryService categoryService;
    private final VibeService vibeService;
    private final GenreMapper genreMapper;
    private final MediaMapper mediaMapper;
    private final ActorMapper actorMapper;
    private final ReviewMapper reviewMapper;
    private final TopListService topListService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void importMedia(Type type, int fromPage, int toPage, String language, String location) {
        deleteAll();
        final Set<String> imdbTop250 = categoryService.getImdbTop250();
        final Set<String> oscarWinningMedia = topListService.getOscarWinningMedia();
        final TmdbMovies tmdbMovies = tmdbService.getTmdbMovies();
        final List<Media> media;
        try (final ForkJoinPool pool = new ForkJoinPool(LIMIT_THREADS)) {
            final List<Movie> movieList =
                    tmdbService.fetchPopularMovies(fromPage, toPage, language, location, pool);
            media = pool.submit(() -> movieList.parallelStream()
                    .map(movieTmdb -> createMovie(language, movieTmdb, tmdbMovies, imdbTop250,
                            oscarWinningMedia))
                    .toList()).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new MovieProcessingException("Failed to process movies with thread pool", e);
        }
        for (int i = 0; i < media.size(); i += BATCH_SIZE) {
            int toIndex = Math.min(i + BATCH_SIZE, media.size());
            mediaRepository.saveAll(media.subList(i, toIndex));
        }
    }

    @Override
    public void start() {
        importMedia(Type.MOVIE, ZERO, LAST_PAGE, LANGUAGE, REGION);
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
        if (mediaRepository != null) {
            mediaRepository.deleteAll();
        }
    }

    private Media createMovie(String language,
                              Movie movieTmdb,
                              TmdbMovies tmdbMovies, Set<String> imdbTop250,
                              Set<String> oscarWinningMedia) {
        final MovieDb movieDb;
        final int movieId = movieTmdb.getId();
        final KeywordResults keywords;
        final Credits credits;
        final List<ReleaseInfo> releaseInfo;
        movieDb = tmdbService.fetchMovieDetails(tmdbMovies, movieId, language);
        final Media media = mediaMapper.toEntity(movieDb);
        credits = tmdbService.fetchMovieCredits(tmdbMovies, movieId, language);
        keywords = tmdbService.fetchKeywords(tmdbMovies, movieId);
        releaseInfo = tmdbService.fetchReleaseInfo(tmdbMovies, movieId);
        final Double voteAverage = movieDb.getVoteAverage();
        final Integer voteCount = movieDb.getVoteCount();
        final Double popularity = movieDb.getPopularity();
        final String title = media.getTitle();
        media.setPosterPath(IMAGE_PATH + movieDb.getPosterPath());
        media.setTrailer(tmdbService.fetchTrailer(tmdbMovies, movieId, language));
        media.setPhotos(tmdbService.fetchPhotos(tmdbMovies, language, movieId));
        media.setReleaseYear(getReleaseYear(movieDb.getReleaseDate()));
        media.setDirector(getDirector(credits.getCrew()));
        media.setActors(getActors(credits.getCast()));
        final Set<GenreEntity> genres = getGenres(movieDb.getGenres());
        media.setGenres(genres);
        media.setReviews(getReviews(tmdbMovies, language, movieId));
        media.setVibes(vibeService.getVibes(releaseInfo, genres));
        media.setCategories(categoryService.putCategories(media.getOverview().toLowerCase(),
                keywords, voteAverage, voteCount, popularity, imdbTop250, title));
        media.setType(putType(media.getDuration()));
        media.setTopLists(topListService.putTopLists(keywords, voteAverage, voteCount, popularity,
                media.getReleaseYear(), oscarWinningMedia, title, movieDb.getBudget(),
                movieDb.getRevenue()));
        return media;
    }

    private Type putType(int duration) {
        if (duration < SHORT_DURATION && duration != ZERO) {
            return Type.SHORTS;
        } else {
            return Type.MOVIE;
        }
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
                    if (cast.getProfilePath() != null) {
                        actor.setPhoto(IMAGE_PATH + cast.getProfilePath());
                    }
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
        return crews.stream().filter(crew -> crew.getJob().equalsIgnoreCase(DIRECTOR)
                || crew.getJob().equalsIgnoreCase(PRODUCER))
                .findFirst()
                .map(Crew::getName)
                .orElse(null);
    }

    private Set<GenreEntity> getGenres(List<Genre> genres) {
        return genres.stream()
                .map(genreMapper::toGenreEntity)
                .collect(Collectors.toSet());
    }

    private Integer getReleaseYear(String releaseDate) {
        if (releaseDate != null && releaseDate.length() == 10) {
            return Integer.parseInt(releaseDate.substring(0, 4));
        }
        return null;
    }
}
