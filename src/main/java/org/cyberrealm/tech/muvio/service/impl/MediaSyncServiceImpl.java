package org.cyberrealm.tech.muvio.service.impl;

import static org.cyberrealm.tech.muvio.model.TopLists.ICONIC_MOVIES_OF_THE_21ST_CENTURY;

import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.TmdbTvSeries;
import info.movito.themoviedbapi.model.core.Genre;
import info.movito.themoviedbapi.model.core.Movie;
import info.movito.themoviedbapi.model.core.NamedIdElement;
import info.movito.themoviedbapi.model.core.TvKeywords;
import info.movito.themoviedbapi.model.core.TvSeries;
import info.movito.themoviedbapi.model.movies.Cast;
import info.movito.themoviedbapi.model.movies.Credits;
import info.movito.themoviedbapi.model.movies.Crew;
import info.movito.themoviedbapi.model.movies.KeywordResults;
import info.movito.themoviedbapi.model.movies.MovieDb;
import info.movito.themoviedbapi.model.movies.ReleaseInfo;
import info.movito.themoviedbapi.model.tv.series.CreatedBy;
import info.movito.themoviedbapi.model.tv.series.TvSeriesDb;
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
import org.cyberrealm.tech.muvio.model.Category;
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
    private static final String DEFAULT_LANGUAGE = "null";
    private static final int DEFAULT_SERIAL_DURATION = 30;
    private static final String TV_PREFICS = "tv";
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
    public void importMedia(Type type, int fromPage, int toPage, String language,
                            String location) {
        deleteAll();
        final Set<String> imdbTop250 = categoryService.getImdbTop250();
        final Set<String> oscarWinningMovie = topListService.getOscarWinningMedia();
        final List<Media> media;
        try (final ForkJoinPool pool = new ForkJoinPool(LIMIT_THREADS)) {
            if (type == Type.MOVIE) {
                final TmdbMovies tmdbMovies = tmdbService.getTmdbMovies();
                final List<Movie> movieList =
                        tmdbService.fetchPopularMovies(fromPage, toPage, language, location, pool);
                media = pool.submit(() -> movieList.parallelStream()
                        .map(movieTmdb -> createMovie(language, movieTmdb, tmdbMovies,
                                imdbTop250,
                                oscarWinningMovie))
                        .toList()).get();
            } else if (type == Type.TV_SHOW) {
                final TmdbTvSeries tmdbTvSeries = tmdbService.getTmdbTvSerials();
                final List<TvSeries> tvSeriesList =
                        tmdbService.fetchPopularTvSerials(fromPage, toPage, language,
                                location, pool);
                media = pool.submit(() -> tvSeriesList.parallelStream()
                        .map(seriesTmdb -> createTvSeries(language, seriesTmdb,
                                tmdbTvSeries,
                                oscarWinningMovie))
                        .toList()).get();
            } else {
                throw new IllegalArgumentException("Unsupported media type: " + type);
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new MediaProcessingException("Failed to process media with thread pool", e);
        }
        for (int i = 0; i < media.size(); i += BATCH_SIZE) {
            int toIndex = Math.min(i + BATCH_SIZE, media.size());
            mediaRepository.saveAll(media.subList(i, toIndex));
        }
    }

    @Override
    public void start() {
        importMedia(Type.MOVIE, ZERO, LAST_PAGE, LANGUAGE, REGION);
        importMedia(Type.TV_SHOW, ZERO, LAST_PAGE, LANGUAGE, REGION);
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
        keywords = tmdbService.fetchMovieKeywords(tmdbMovies, movieId);
        releaseInfo = tmdbService.fetchReleaseInfo(tmdbMovies, movieId);
        final Double voteAverage = movieDb.getVoteAverage();
        final Integer voteCount = movieDb.getVoteCount();
        final Double popularity = movieDb.getPopularity();
        final String title = media.getTitle();
        media.setPosterPath(IMAGE_PATH + movieDb.getPosterPath());
        media.setTrailer(tmdbService.fetchMovieTrailer(tmdbMovies, movieId, language));
        media.setPhotos(tmdbService.fetchMoviePhotos(tmdbMovies, DEFAULT_LANGUAGE, movieId));
        media.setReleaseYear(getReleaseYear(movieDb.getReleaseDate()));
        media.setDirector(getMovieDirector(credits.getCrew()));
        media.setActors(getMovieActors(credits.getCast()));
        final Set<GenreEntity> genres = getGenres(movieDb.getGenres());
        media.setGenres(genres);
        media.setReviews(getReviews(() ->
                tmdbService.fetchMovieReviews(tmdbMovies, language, movieId)));
        media.setVibes(vibeService.getVibes(releaseInfo, genres));
        media.setCategories(categoryService.putCategories(media.getOverview().toLowerCase(),
                keywords, voteAverage, voteCount, popularity, imdbTop250, title));
        media.setType(putType(media.getDuration()));
        media.setTopLists(topListService.putTopLists(keywords, voteAverage, voteCount, popularity,
                media.getReleaseYear(), oscarWinningMedia, title, movieDb.getBudget(),
                movieDb.getRevenue()));
        return media;
    }

    private Media createTvSeries(String language,
                                 TvSeries tvSeriesTmdb,
                                 TmdbTvSeries tmdbTvSeries, Set<String> oscarWinningMedia) {
        final TvSeriesDb tvSeriesDb;
        final int seriesId = tvSeriesTmdb.getId();
        final TvKeywords keywords;
        final info.movito.themoviedbapi.model.tv.core.credits.Credits credits;
        tvSeriesDb = tmdbService.fetchTvSerialsDetails(tmdbTvSeries, seriesId, language);
        final Media media = mediaMapper.toEntity(tvSeriesDb);
        credits = tmdbService.fetchTvSerialsCredits(tmdbTvSeries, seriesId, language);
        keywords = tmdbService.fetchTvSerialsKeywords(tmdbTvSeries, seriesId);
        final Double voteAverage = tvSeriesDb.getVoteAverage();
        final Integer voteCount = tvSeriesDb.getVoteCount();
        final Double popularity = tvSeriesDb.getPopularity();
        final String title = media.getTitle();
        media.setId(TV_PREFICS + media.getId());
        media.setDuration(getDurations(tvSeriesDb));
        media.setPosterPath(IMAGE_PATH + tvSeriesDb.getPosterPath());
        media.setTrailer(tmdbService.fetchTvSerialsTrailer(tmdbTvSeries, seriesId, language));
        media.setPhotos(tmdbService.fetchTvSerialsPhotos(tmdbTvSeries, DEFAULT_LANGUAGE, seriesId));
        media.setReleaseYear(getReleaseYear(tvSeriesDb.getFirstAirDate()));
        media.setDirector(getTvDirector(tvSeriesDb.getCreatedBy()));
        media.setActors(getTvActors(credits.getCast()));
        final Set<GenreEntity> genres = getGenres(tvSeriesDb.getGenres());
        media.setGenres(genres);
        media.setReviews(getReviews(() ->
                tmdbService.fetchTvSerialsReviews(tmdbTvSeries,language,seriesId)));
        //media.setCategories(categoryService.putCategories(media.getOverview().toLowerCase(),
        //keywords, voteAverage, voteCount, popularity, popularShows, title));
        //By default
        media.setCategories(Set.of(Category.SPORT_LIFE_MOVIES));
        media.setTopLists(Set.of(ICONIC_MOVIES_OF_THE_21ST_CENTURY));

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

    private Map<String, Actor> getTvActors(
            List<info.movito.themoviedbapi.model.tv.core.credits.Cast> casts) {
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
        if (releaseDate != null && releaseDate.length() == 10) {
            return Integer.parseInt(releaseDate.substring(0, 4));
        }
        return null;
    }
}
