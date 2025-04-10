package org.cyberrealm.tech.muvio.service.impl;

import info.movito.themoviedbapi.model.core.NamedIdElement;
import info.movito.themoviedbapi.model.keywords.Keyword;
import info.movito.themoviedbapi.model.movies.Cast;
import info.movito.themoviedbapi.model.movies.Credits;
import info.movito.themoviedbapi.model.movies.Crew;
import info.movito.themoviedbapi.model.movies.MovieDb;
import info.movito.themoviedbapi.model.tv.series.CreatedBy;
import info.movito.themoviedbapi.model.tv.series.TvSeriesDb;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.cyberrealm.tech.muvio.mapper.ActorMapper;
import org.cyberrealm.tech.muvio.mapper.MediaMapper;
import org.cyberrealm.tech.muvio.mapper.ReviewMapper;
import org.cyberrealm.tech.muvio.model.Actor;
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

@RequiredArgsConstructor
@Service
public class MediaSyncServiceImpl implements MediaSyncService, SmartLifecycle {
    private static final String IMAGE_PATH = "https://image.tmdb.org/t/p/w500";
    private static final int BATCH_SIZE = 500;
    private static final int ZERO = 0;
    private static final int ONE = 1;
    private static final int MAX_NUMBER_OF_ACTORS = 3;
    private static final int LAST_PAGE = 500;
    private static final int FIRST_YEAR = 1946;
    private static final String EMPTY = "";
    private static final String REGION = "US";
    private static final String LANGUAGE = "en";
    private static final String DIRECTOR = "Director";
    private static final String DEFAULT_LANGUAGE = "null";
    private static final String TV = "TV";
    private boolean isRunning;
    private final TmDbService tmdbService;
    private final MediaRepository mediaRepository;
    private final ActorRepository actorRepository;
    private final CategoryService categoryService;
    private final VibeService vibeService;
    private final MediaMapper mediaMapper;
    private final ActorMapper actorMapper;
    private final ReviewMapper reviewMapper;
    private final TopListService topListService;
    private final AwardService awardService;

    @Override
    public void importMedia(String language, String location, int currentYear,
                            Set<String> imdbTop250, Set<String> winningMedia,
                            Map<Integer, Actor> actorStorage, Map<String, Media> mediaStorage,
                            boolean isMovies) {
        final Set<Integer> ids = IntStream.rangeClosed(ZERO, LAST_PAGE).parallel()
                .mapToObj(page -> isMovies
                        ? tmdbService.fetchPopularMovies(language, page, location)
                        : tmdbService.fetchPopularTvSerials(language, page))
                .flatMap(Collection::stream).filter(id -> isNewIds(id, isMovies, mediaStorage))
                .collect(Collectors.toSet());
        ids.parallelStream().forEach(id -> {
            final Media media = isMovies
                    ? createMovie(language, currentYear, id, imdbTop250,
                    winningMedia, actorStorage)
                    : createTvSeries(language, currentYear, id, imdbTop250,
                    winningMedia, actorStorage);
            mediaStorage.put(media.getId(), media);
        });
    }

    @Override
    public void importMediaByFilter(String language, int currentYear, Set<String> imdbTop250,
                                    Set<String> winningMedia, Map<String, Media> mediaStorage,
                                    Map<Integer, Actor> actorStorage, boolean isMovies) {
        final Set<Integer> ids =
                IntStream.rangeClosed(FIRST_YEAR, currentYear).parallel()
                        .boxed().flatMap(year -> IntStream.iterate(
                                        ONE, page -> page <= LAST_PAGE, page -> page + ONE)
                                .mapToObj(page -> (isMovies
                                        ? tmdbService.getFilteredMovies(year, page)
                                        : tmdbService.getFilteredTvShows(year, page)))
                                .takeWhile(set -> !set.isEmpty()))
                        .flatMap(Collection::stream)
                        .filter(id -> isNewIds(id, isMovies, mediaStorage))
                        .collect(Collectors.toSet());
        ids.parallelStream().forEach(id -> {
            final Media newMedia = isMovies
                    ? createMovie(language, currentYear, id, imdbTop250, winningMedia,
                    actorStorage)
                    : createTvSeries(language, currentYear, id, imdbTop250,
                    winningMedia, actorStorage);
            mediaStorage.put(newMedia.getId(), newMedia);
        });
    }

    @Override
    public void importByFindingTitles(String language, String region, int currentYear,
                                      Map<Integer, Actor> actorStorage,
                                      Map<String, Media> mediaStorage, Set<String> imdbTop250,
                                      Set<String> winningMedia, boolean isMovies) {
        final Set<Integer> mediaId = new HashSet<>();
        findMediasIdsByTitles(language, region, imdbTop250, isMovies, mediaId);
        findMediasIdsByTitles(language, region, winningMedia, isMovies, mediaId);
        if (mediaId.isEmpty()) {
            return;
        }
        mediaId.parallelStream().filter(id -> isNewIds(id, isMovies, mediaStorage))
                .forEach(id -> {
                    final Media newMedia = isMovies
                            ? createMovie(language, currentYear, id, imdbTop250, winningMedia,
                            actorStorage)
                            : createTvSeries(language, currentYear, id, imdbTop250,
                            winningMedia, actorStorage);
                    mediaStorage.put(newMedia.getId(), newMedia);
                });
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
    public void saveAll(Map<Integer, Actor> actorStorage, Map<String, Media> mediaStorage) {
        List<Actor> actorList = new ArrayList<>(actorStorage.values());
        for (int i = ZERO; i < actorList.size(); i += BATCH_SIZE) {
            int toIndex = Math.min(i + BATCH_SIZE, actorList.size());
            actorRepository.saveAll(actorList.subList(i, toIndex));
        }
        List<Media> mediaList = new ArrayList<>(mediaStorage.values());
        for (int i = ZERO; i < mediaList.size(); i += BATCH_SIZE) {
            int toIndex = Math.min(i + BATCH_SIZE, mediaList.size());
            mediaRepository.saveAll(mediaList.subList(i, toIndex));
        }
    }

    @Override
    public void start() {
        final Map<Integer, Actor> actorStorage = new ConcurrentHashMap<>();
        final Map<String, Media> mediaStorage = new ConcurrentHashMap<>();
        final int currentYear = Year.now().getValue();
        final Set<String> imdbTop250Movies = awardService.getImdbTop250Movies();
        final Set<String> oscarWinningMovies = awardService.getOscarWinningMovies();
        final Set<String> imdbTop250TvShows = awardService.getImdbTop250TvShows();
        final Set<String> emmyWinningTvShows = awardService.getEmmyWinningTvShows();
        importMedia(LANGUAGE, REGION, currentYear, imdbTop250Movies, oscarWinningMovies,
                actorStorage, mediaStorage, true);
        importMedia(LANGUAGE, REGION, currentYear, imdbTop250TvShows, emmyWinningTvShows,
                actorStorage, mediaStorage, false);
        deleteAll();
        saveAll(actorStorage, mediaStorage);
        isRunning = true;
    }

    @Scheduled(cron = "${sync.cron.time}")
    public void worker() {
        final Map<Integer, Actor> actorStorage = new ConcurrentHashMap<>();
        final Map<String, Media> mediaStorage = new ConcurrentHashMap<>();
        final int currentYear = Year.now().getValue();
        final Set<String> imdbTop250Movies = awardService.getImdbTop250Movies();
        final Set<String> oscarWinningMovies = awardService.getOscarWinningMovies();
        final Set<String> imdbTop250TvShows = awardService.getImdbTop250TvShows();
        final Set<String> emmyWinningTvShows = awardService.getEmmyWinningTvShows();
        importMedia(LANGUAGE, REGION, currentYear, imdbTop250Movies, oscarWinningMovies,
                actorStorage, mediaStorage, true);
        importMedia(LANGUAGE, REGION, currentYear, imdbTop250TvShows, emmyWinningTvShows,
                actorStorage, mediaStorage, false);
        importByFindingTitles(LANGUAGE, REGION, currentYear, actorStorage, mediaStorage,
                imdbTop250Movies, oscarWinningMovies, true);
        importByFindingTitles(LANGUAGE, REGION, currentYear, actorStorage, mediaStorage,
                imdbTop250TvShows, emmyWinningTvShows, false);
        importMediaByFilter(LANGUAGE, currentYear, imdbTop250Movies, oscarWinningMovies,
                mediaStorage, actorStorage, true);
        importMediaByFilter(LANGUAGE, currentYear, imdbTop250TvShows, emmyWinningTvShows,
                mediaStorage, actorStorage, false);
        deleteAll();
        saveAll(actorStorage, mediaStorage);
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

    private Media createMovie(String language, int currentYear,
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
        media.setTrailer(tmdbService.fetchMovieTrailer(movieId, language));
        media.setPhotos(tmdbService.fetchMoviePhotos(DEFAULT_LANGUAGE, movieId));
        media.setDirector(getMovieDirector(credits.getCrew()));
        media.setActors(getMovieActors(credits.getCast(), actors));
        media.setReviews(getReviews(() ->
                tmdbService.fetchMovieReviews(language, movieId)));
        media.setVibes(vibeService.getVibes(tmdbService.fetchTmDbMovieRatings(movieId),
                media.getGenres()));
        media.setCategories(categoryService.putCategories(media.getOverview().toLowerCase(),
                keywords, voteAverage, voteCount, popularity, imdbTop250, title));
        media.setTopLists(topListService.putTopLists(keywords, voteAverage, voteCount, popularity,
                media.getReleaseYear(), oscarWinningMedia, title, movieDb.getBudget(),
                movieDb.getRevenue()));
        return media;
    }

    private Media createTvSeries(String language, int currentYear,
                                 Integer seriesId,
                                 Set<String> imdbTop250,
                                 Set<String> emmyWinningMedia, Map<Integer, Actor> actors) {
        final List<Keyword> keywords = tmdbService.fetchTvSerialsKeywords(seriesId)
                .getResults();
        final TvSeriesDb tvSeriesDb = tmdbService.fetchTvSerialsDetails(seriesId, language);
        final Media media = mediaMapper.toEntity(tvSeriesDb);
        final info.movito.themoviedbapi.model.tv.core.credits.Credits credits
                = tmdbService.fetchTvSerialsCredits(seriesId, language);
        final Double voteAverage = media.getRating();
        final Integer voteCount = tvSeriesDb.getVoteCount();
        final Double popularity = tvSeriesDb.getPopularity();
        final String title = media.getTitle();
        media.setPosterPath(IMAGE_PATH + tvSeriesDb.getPosterPath());
        media.setTrailer(tmdbService.fetchTvSerialsTrailer(seriesId, language));
        media.setPhotos(tmdbService.fetchTvSerialsPhotos(DEFAULT_LANGUAGE, seriesId));
        media.setDirector(getTvDirector(tvSeriesDb.getCreatedBy()));
        media.setActors(getTvActors(credits.getCast(), actors));
        media.setReviews(getReviews(() ->
                tmdbService.fetchTvSerialsReviews(language, seriesId)));
        media.setCategories(categoryService.putCategories(media.getOverview().toLowerCase(),
                keywords, voteAverage, voteCount, popularity, imdbTop250, title));
        media.setTopLists(topListService.putTopListsForTvShow(keywords, voteAverage, voteCount,
                popularity, media.getReleaseYear(), emmyWinningMedia, title));
        media.setVibes(vibeService.getVibes(tmdbService.fetchTmDbTvRatings(seriesId),
                media.getGenres()));
        return media;
    }

    private List<Review> getReviews(
            Supplier<List<info.movito.themoviedbapi.model.core.Review>> reviewsSupplier
    ) {
        return reviewsSupplier.get().stream()
                .map(reviewMapper::toEntity)
                .toList();
    }

    private List<RoleActor> getMovieActors(List<Cast> casts, Map<Integer, Actor> actors) {
        return casts.stream().limit(MAX_NUMBER_OF_ACTORS).map(cast -> {
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
        return casts.stream().limit(MAX_NUMBER_OF_ACTORS).map(cast -> {
            final RoleActor roleActor = new RoleActor();
            roleActor.setRole(cast.getCharacter());
            roleActor.setActor(actors.computeIfAbsent(cast.getId(),
                    id -> actorMapper.toActorEntity(cast)));
            return roleActor;
        }).toList();
    }

    private String getMovieDirector(List<Crew> crews) {
        return crews.stream().filter(crew -> crew.getJob().equalsIgnoreCase(DIRECTOR))
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

    private void findMediasIdsByTitles(String language, String region, Set<String> titles,
                                       boolean isMovies, Set<Integer> mediaId) {
        if (!titles.isEmpty()) {
            titles.parallelStream()
                    .map(title -> isMovies
                            ? tmdbService.searchMovies(title, language, region)
                            : tmdbService.searchTvSeries(title, language))
                    .filter(Optional::isPresent)
                    .forEach(id -> mediaId.add(id.get()));
        }
    }

    private boolean isNewIds(int id, boolean isMovies, Map<String, Media> mediaStorage) {
        return !mediaStorage.containsKey((isMovies ? EMPTY : TV) + id);
    }
}
