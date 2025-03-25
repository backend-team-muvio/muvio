package org.cyberrealm.tech.muvio.service.impl;

import info.movito.themoviedbapi.model.core.Genre;
import info.movito.themoviedbapi.model.core.Movie;
import info.movito.themoviedbapi.model.core.NamedIdElement;
import info.movito.themoviedbapi.model.core.TvSeries;
import info.movito.themoviedbapi.model.keywords.Keyword;
import info.movito.themoviedbapi.model.movies.Cast;
import info.movito.themoviedbapi.model.movies.Credits;
import info.movito.themoviedbapi.model.movies.Crew;
import info.movito.themoviedbapi.model.movies.MovieDb;
import info.movito.themoviedbapi.model.tv.series.CreatedBy;
import info.movito.themoviedbapi.model.tv.series.TvSeriesDb;
import java.time.Year;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.cyberrealm.tech.muvio.exception.MediaProcessingException;
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
import org.cyberrealm.tech.muvio.service.TmDbService;
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
            Math.min(20, Runtime.getRuntime().availableProcessors() * 2);
    private static final int BATCH_SIZE = 100;
    private static final int ZERO = 0;
    private static final int ONE = 1;
    private static final int LAST_PAGE = 50;
    private static final String REGION = "US";
    private static final String LANGUAGE = "en";
    private static final String DIRECTOR = "Director";
    private static final String PRODUCER = "Producer";
    private static final String DEFAULT_LANGUAGE = "null";
    private static final int CURRENT_YEAR = Year.now().getValue();
    private static final String POINT = ".";
    private static final String UNDERSCORE = "_";
    private static final int FOUR = 4;
    private static final int TEN = 10;
    private static final int DEFAULT_SERIAL_DURATION = 30;
    private boolean isRunning;
    private final TmDbService tmdbService;
    private final MediaRepository mediaRepository;
    private final ActorRepository actorRepository;
    private final CategoryService categoryService;
    private final VibeService vibeService;
    private final GenreMapper genreMapper;
    private final MediaMapper mediaMapper;
    private final ActorMapper actorMapper;
    private final ReviewMapper reviewMapper;
    private final TopListService topListService;
    private final Set<String> imdbTop250Movies;
    private final Set<String> imdbTop250TvShows;
    private final Set<String> oscarWinningMedia;
    private final Set<String> emmyWinningMedia;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void importMedia(Type type, int fromPage, int toPage, String language,
                            String location, Set<String> imdbTop250,
                            Set<String> oscarWinningMovie) {
        final List<Media> media;
        try (final ForkJoinPool pool = new ForkJoinPool(LIMIT_THREADS)) {
            if (type == Type.MOVIE) {
                final List<Movie> movieList =
                        tmdbService.fetchPopularMovies(fromPage, toPage, language, location, pool);
                media = pool.submit(() -> movieList.parallelStream()
                        .map(movieTmDb -> createMovie(language, movieTmDb, imdbTop250,
                                oscarWinningMovie))
                        .toList()).get();
            } else if (type == Type.TV_SHOW) {
                final List<TvSeries> tvSeriesList =
                        tmdbService.fetchPopularTvSerials(fromPage, toPage, language,
                                location, pool);
                media = pool.submit(() -> tvSeriesList.parallelStream()
                        .map(seriesTmDb -> createTvSeries(language, seriesTmDb,
                                imdbTop250, oscarWinningMovie))
                        .toList()).get();
            } else {
                throw new IllegalArgumentException("Unsupported media type: " + type);
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new MediaProcessingException("Failed to process media with thread pool", e);
        }
        for (int i = ZERO; i < media.size(); i += BATCH_SIZE) {
            int toIndex = Math.min(i + BATCH_SIZE, media.size());
            mediaRepository.saveAll(media.subList(i, toIndex));
        }
    }

    @Override
    public void start() {
        deleteAll();
        importMedia(Type.MOVIE, ZERO, LAST_PAGE, LANGUAGE, REGION, imdbTop250Movies,
                oscarWinningMedia);
        importMedia(Type.TV_SHOW, ZERO, LAST_PAGE, LANGUAGE, REGION, imdbTop250TvShows,
                emmyWinningMedia);
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
                              Movie movieTmDb,
                              Set<String> imdbTop250,
                              Set<String> oscarWinningMedia) {
        final int movieId = movieTmDb.getId();
        final MovieDb movieDb = tmdbService.fetchMovieDetails(movieId, language);
        final List<Keyword> keywords = tmdbService.fetchMovieKeywords(movieId)
                .getKeywords();
        final Credits credits = tmdbService.fetchMovieCredits(movieId, language);
        final Media media = mediaMapper.toEntity(movieDb);
        final Double voteAverage = media.getRating();
        final Integer voteCount = movieDb.getVoteCount();
        final Double popularity = movieDb.getPopularity();
        final String title = media.getTitle();
        media.setPosterPath(IMAGE_PATH + movieDb.getPosterPath());
        media.setTrailer(tmdbService.fetchMovieTrailer(movieId, language));
        media.setPhotos(tmdbService.fetchMoviePhotos(DEFAULT_LANGUAGE, movieId));
        media.setReleaseYear(getReleaseYear(movieDb.getReleaseDate()));
        media.setDirector(getMovieDirector(credits.getCrew()));
        media.setActors(getMovieActors(credits.getCast()));
        final Set<GenreEntity> genres = getGenres(movieDb.getGenres());
        media.setGenres(genres);
        media.setReviews(getReviews(() ->
                tmdbService.fetchMovieReviews(language, movieId)));
        media.setVibes(vibeService.getVibes(tmdbService.fetchTmDbMovieRatings(movieId), genres));
        media.setCategories(categoryService.putCategories(media.getOverview().toLowerCase(),
                keywords, voteAverage, voteCount, popularity, imdbTop250, title));
        media.setType(putType(media.getDuration()));
        media.setTopLists(topListService.putTopLists(keywords, voteAverage, voteCount, popularity,
                media.getReleaseYear(), oscarWinningMedia, title, movieDb.getBudget(),
                movieDb.getRevenue()));
        return media;
    }

    private Media createTvSeries(String language,
                                 TvSeries tvSeriesTmDb,
                                 Set<String> imdbTop250,
                                 Set<String> oscarWinningMedia) {
        final TvSeriesDb tvSeriesDb;
        final int seriesId = tvSeriesTmDb.getId();
        final List<Keyword> keywords = tmdbService.fetchTvSerialsKeywords(seriesId)
                .getResults();
        final info.movito.themoviedbapi.model.tv.core.credits.Credits credits;
        tvSeriesDb = tmdbService.fetchTvSerialsDetails(seriesId, language);
        final Media media = mediaMapper.toEntity(tvSeriesDb);
        credits = tmdbService.fetchTvSerialsCredits(seriesId, language);
        final Double voteAverage = media.getRating();
        final Integer voteCount = tvSeriesDb.getVoteCount();
        final Double popularity = tvSeriesDb.getPopularity();
        final String title = media.getTitle();
        media.setDuration(getDurations(tvSeriesDb));
        media.setPosterPath(IMAGE_PATH + tvSeriesDb.getPosterPath());
        media.setTrailer(tmdbService.fetchTvSerialsTrailer(seriesId, language));
        media.setPhotos(tmdbService.fetchTvSerialsPhotos(DEFAULT_LANGUAGE, seriesId));
        media.setReleaseYear(getReleaseYear(tvSeriesDb.getFirstAirDate()));
        media.setDirector(getTvDirector(tvSeriesDb.getCreatedBy()));
        media.setActors(getTvActors(credits.getCast()));
        final Set<GenreEntity> genres = getGenres(tvSeriesDb.getGenres());
        media.setGenres(genres);
        media.setReviews(getReviews(() ->
                tmdbService.fetchTvSerialsReviews(language, seriesId)));
        if (media.getReleaseYear() == null) {
            media.setReleaseYear(CURRENT_YEAR);
        }
        media.setCategories(categoryService.putCategories(media.getOverview().toLowerCase(),
                keywords, voteAverage, voteCount, popularity, imdbTop250, title));
        media.setTopLists(topListService.putTopListsForTvShow(keywords, voteAverage, voteCount,
                popularity, media.getReleaseYear(), oscarWinningMedia, title));
        media.setVibes(vibeService.getVibes(tmdbService.fetchTmDbTvRatings(seriesId), genres));
        media.setType(Type.TV_SHOW);
        return media;
    }

    private Integer getDurations(TvSeriesDb tvSeriesDb) {
        return tvSeriesDb.getEpisodeRunTime().stream()
                .findFirst()
                .orElse(DEFAULT_SERIAL_DURATION);
    }

    private Type putType(int duration) {
        if (duration < SHORT_DURATION && duration != ZERO) {
            return Type.SHORTS;
        } else {
            return Type.MOVIE;
        }
    }

    private List<Review> getReviews(
            Supplier<List<info.movito.themoviedbapi.model.core.Review>> reviewsSupplier
    ) {
        return reviewsSupplier.get().stream()
                .map(reviewMapper::toEntity)
                .toList();
    }

    private Map<String, Actor> getMovieActors(List<Cast> casts) {
        final Set<Actor> newActors = new HashSet<>();
        final Map<String, Actor> actorsMap = casts.stream().collect(Collectors.toMap(cast ->
                        cast.getCharacter().replace(POINT, UNDERSCORE), cast -> {
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

    private Map<String, Actor> getTvActors(
            List<info.movito.themoviedbapi.model.tv.core.credits.Cast> casts) {
        final Set<Actor> newActors = new HashSet<>();
        final Map<String, Actor> actorsMap = casts.stream().collect(Collectors.toMap(cast ->
                        cast.getCharacter().replace(POINT, UNDERSCORE), cast -> {
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

    private String getMovieDirector(List<Crew> crews) {
        return crews.stream().filter(crew -> crew.getJob().equalsIgnoreCase(DIRECTOR)
                || crew.getJob().equalsIgnoreCase(PRODUCER))
                .findFirst()
                .map(Crew::getName)
                .orElse(null);
    }

    private String getTvDirector(List<CreatedBy> creators) {
        return creators.stream()
                .map(NamedIdElement::getName)
                .findFirst()
                .orElse(null);
    }

    private Set<GenreEntity> getGenres(List<Genre> genres) {
        return genres.stream()
                .map(genreMapper::toGenreEntity)
                .collect(Collectors.toSet());
    }

    private Integer getReleaseYear(String releaseDate) {
        if (releaseDate != null && releaseDate.length() == TEN) {
            return Integer.parseInt(releaseDate.substring(ZERO, FOUR));
        }
        return null;
    }
}
