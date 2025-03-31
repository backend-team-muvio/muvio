package org.cyberrealm.tech.muvio.service.impl;

import info.movito.themoviedbapi.model.core.Genre;
import info.movito.themoviedbapi.model.core.IdElement;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
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
import org.cyberrealm.tech.muvio.model.RoleActor;
import org.cyberrealm.tech.muvio.model.Type;
import org.cyberrealm.tech.muvio.repository.actors.ActorRepository;
import org.cyberrealm.tech.muvio.repository.media.MediaRepository;
import org.cyberrealm.tech.muvio.service.AwardService;
import org.cyberrealm.tech.muvio.service.CategoryService;
import org.cyberrealm.tech.muvio.service.MediaSyncService;
import org.cyberrealm.tech.muvio.service.TmDbService;
import org.cyberrealm.tech.muvio.service.TopListService;
import org.cyberrealm.tech.muvio.service.VibeService;
import org.springframework.context.SmartLifecycle;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MediaSyncServiceImpl implements MediaSyncService, SmartLifecycle {
    private static final int SHORT_DURATION = 40;
    private static final String IMAGE_PATH = "https://image.tmdb.org/t/p/w500";
    private static final int LIMIT_THREADS =
            Math.min(20, Runtime.getRuntime().availableProcessors() * 2);
    private static final int BATCH_SIZE = 500;
    private static final int ZERO = 0;
    private static final int ONE = 1;
    private static final int LAST_PAGE = 500;
    private static final String REGION = "US";
    private static final String LANGUAGE = "en";
    private static final String DIRECTOR = "Director";
    private static final String PRODUCER = "Producer";
    private static final String DEFAULT_LANGUAGE = "null";
    private static final int CURRENT_YEAR = Year.now().getValue();
    private static final int FIRST_YEAR = 1920;
    private static final int TEN = 10;
    private static final int DEFAULT_SERIAL_DURATION = 30;
    private static final double MIN_RATING = 6;
    private static final double MIN_VOTE_COUNT = 100;
    private static final String TV = "TV";
    private static final String CRON_WEEKLY = "0 0 3 ? * MON";
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
    private final AwardService awardService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void importMedia(Type type, int fromPage, int toPage, String language, String location,
                            Set<String> imdbTop250, Set<String> winningMedia, Map<Integer,
                    Actor> actors, Map<String, Media> media) {
        try (final ForkJoinPool pool = new ForkJoinPool(LIMIT_THREADS)) {
            final List<Media> mediaNew;
            if (type == Type.MOVIE) {
                final List<Movie> movieList =
                        tmdbService.fetchPopularMovies(fromPage, toPage, language, location, pool);
                mediaNew = pool.submit(() -> movieList.parallelStream()
                        .map(movieTmDb -> createMovie(language, movieTmDb.getId(), imdbTop250,
                                winningMedia, actors))
                        .collect(Collectors.toCollection(ArrayList::new))).get();
                addAdditionalMedia(imdbTop250, winningMedia, mediaNew, pool,
                        title -> tmdbService.searchMovies(title, language, language),
                        id -> createMovie(language, id, imdbTop250, winningMedia, actors));
                mediaNew.forEach(media1 -> media.putIfAbsent(media1.getId(), media1));
                for (int i = FIRST_YEAR; i <= CURRENT_YEAR; i++) {
                    Set<Integer> ids = tmdbService.getFilteredMovies(i, MIN_RATING, MIN_VOTE_COUNT)
                            .stream().map(IdElement::getId).collect(Collectors.toSet());
                    ids.removeIf(id -> media.containsKey(id.toString()));
                    final ConcurrentHashMap<String, Media> collects = pool.submit(
                            () -> ids.parallelStream().map(id -> createMovie(
                                    language, id, imdbTop250, winningMedia, actors)).collect(
                                            () -> new ConcurrentHashMap<String, Media>(),
                                            (map, movie) -> map.put(movie.getId(), movie),
                                            ConcurrentHashMap::putAll)).get();
                    media.putAll(collects);
                }
                pool.shutdown();
            } else if (type == Type.TV_SHOW) {
                final List<TvSeries> tvSeriesList =
                        tmdbService.fetchPopularTvSerials(fromPage, toPage, language,
                                location, pool);
                mediaNew = pool.submit(() -> tvSeriesList.parallelStream()
                        .map(seriesTmDb -> createTvSeries(language, seriesTmDb.getId(),
                                imdbTop250, winningMedia, actors))
                        .collect(Collectors.toCollection(ArrayList::new))).get();
                addAdditionalMedia(imdbTop250, winningMedia, mediaNew, pool,
                        title -> tmdbService.searchTvSeries(title, language),
                        id -> createTvSeries(language, id, imdbTop250, winningMedia, actors));
                mediaNew.forEach(media1 -> media.putIfAbsent(media1.getId(), media1));
                for (int i = FIRST_YEAR; i <= CURRENT_YEAR; i++) {
                    Set<Integer> ids = tmdbService.getFilteredTvShows(i, MIN_RATING,
                                    MIN_VOTE_COUNT).stream().map(IdElement::getId)
                            .collect(Collectors.toSet());
                    ids.removeIf(id -> media.containsKey(TV + id));
                    final ConcurrentHashMap<String, Media> collects = pool.submit(
                            () -> ids.parallelStream().map(id -> createTvSeries(
                                    language, id, imdbTop250, winningMedia, actors)).collect(
                                            () -> new ConcurrentHashMap<String, Media>(),
                                        (map, serials) -> map.put(serials.getId(), serials),
                                    ConcurrentHashMap::putAll)).get();
                    media.putAll(collects);
                }
                pool.shutdown();
            } else {
                throw new IllegalArgumentException("Unsupported media type: " + type);
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new MediaProcessingException("Failed to process media with thread pool", e);
        }
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

    @Override
    public void saveAll(Map<Integer, Actor> actors, Map<String, Media> medias) {
        List<Actor> actorList = new ArrayList<>(actors.values());
        for (int i = 0; i < actorList.size(); i += BATCH_SIZE) {
            int toIndex = Math.min(i + BATCH_SIZE, actorList.size());
            actorRepository.saveAll(actorList.subList(i, toIndex));
        }
        List<Media> mediaList = new ArrayList<>(medias.values());
        for (int i = 0; i < mediaList.size(); i += BATCH_SIZE) {
            int toIndex = Math.min(i + BATCH_SIZE, mediaList.size());
            mediaRepository.saveAll(mediaList.subList(i, toIndex));
        }
    }

    @Scheduled(cron = CRON_WEEKLY)
    @Override
    public void start() {
        final Map<Integer, Actor> actors = new ConcurrentHashMap<>();
        final Map<String, Media> medias = new ConcurrentHashMap<>();
        importMedia(Type.MOVIE, ZERO, LAST_PAGE, LANGUAGE, REGION,
                awardService.getImdbTop250Movies(), awardService.getOscarWinningMovies(),
                actors, medias);
        importMedia(Type.TV_SHOW, ZERO, LAST_PAGE, LANGUAGE, REGION,
                awardService.getImdbTop250TvShows(), awardService.getEmmyWinningTvShows(),
                actors, medias);
        deleteAll();
        saveAll(actors, medias);
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

    private Media createMovie(String language,
                              Integer movieId,
                              Set<String> imdbTop250,
                              Set<String> oscarWinningMedia, Map<Integer, Actor> actors) {
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
        media.setActors(getMovieActors(credits.getCast(), actors));
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
                                 Integer seriesId,
                                 Set<String> imdbTop250,
                                 Set<String> emmyWinningMedia, Map<Integer, Actor> actors) {
        final List<Keyword> keywords = tmdbService.fetchTvSerialsKeywords(seriesId)
                .getResults();
        final info.movito.themoviedbapi.model.tv.core.credits.Credits credits;
        final TvSeriesDb tvSeriesDb = tmdbService.fetchTvSerialsDetails(seriesId, language);
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
        media.setActors(getTvActors(credits.getCast(), actors));
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
                popularity, media.getReleaseYear(), emmyWinningMedia, title));
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

    private List<RoleActor> getMovieActors(List<Cast> casts, Map<Integer, Actor> actors) {
        return casts.stream().map(cast -> {
            final RoleActor roleActor = new RoleActor();
            roleActor.setRole(cast.getCharacter());
            roleActor.setActor(actors.computeIfAbsent(
                    cast.getId(), id -> actorMapper.toActorEntity(cast)));
            return roleActor;
        }).toList();
    }

    private List<RoleActor> getTvActors(
            List<info.movito.themoviedbapi.model.tv.core.credits.Cast> casts,
            Map<Integer, Actor> actors) {
        return casts.stream().map(cast -> {
            final RoleActor roleActor = new RoleActor();
            roleActor.setRole(cast.getCharacter());
            roleActor.setActor(actors.computeIfAbsent(cast.getId(),
                    id -> actorMapper.toActorEntity(cast)));
            return roleActor;
        }).toList();
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
        return Optional.ofNullable(releaseDate).filter(date -> date.length() == TEN)
                .map(date -> Integer.parseInt(date.substring(0, 4))).orElse(CURRENT_YEAR);
    }

    private void addAdditionalMedia(Set<String> imdbTop250, Set<String> winningMedia,
                                    List<Media> media, ForkJoinPool pool,
                                    Function<String, Optional<Integer>> searchFunction,
                                    Function<Integer, Media> createFunction) {
        if (!imdbTop250.isEmpty()) {
            final Set<Media> mediaSet;
            try {
                final Set<Integer> mediaId = pool.submit(() -> imdbTop250.parallelStream().map(
                                searchFunction).filter(Optional::isPresent).map(Optional::get)
                        .collect(Collectors.toSet())).get();
                mediaSet = pool.submit(() -> mediaId.parallelStream()
                        .map(createFunction).collect(Collectors.toSet())).get();
            } catch (InterruptedException | ExecutionException e) {
                throw new MediaProcessingException(
                        "Failed to process add additional top250 media with thread pool", e);
            }
            media.addAll(mediaSet);
        }
        if (!winningMedia.isEmpty()) {
            final Set<Media> mediaSet;
            try {
                final Set<Integer> mediaId = pool.submit(() -> winningMedia.parallelStream().map(
                                searchFunction)
                        .filter(Optional::isPresent).map(Optional::get)
                        .collect(Collectors.toSet())).get();
                mediaSet = pool.submit(() -> mediaId.parallelStream()
                        .map(createFunction).collect(Collectors.toSet())).get();
            } catch (InterruptedException | ExecutionException e) {
                throw new MediaProcessingException(
                        "Failed to process add additional winning media with thread pool", e);
            }
            media.addAll(mediaSet);
        }
    }
}
