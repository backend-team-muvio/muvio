package org.cyberrealm.tech.muvio.service.impl;

import static org.cyberrealm.tech.muvio.common.Constants.DIRECTOR;
import static org.cyberrealm.tech.muvio.common.Constants.LANGUAGE_EN;

import info.movito.themoviedbapi.model.core.NamedIdElement;
import info.movito.themoviedbapi.model.keywords.Keyword;
import info.movito.themoviedbapi.model.movies.Cast;
import info.movito.themoviedbapi.model.movies.Credits;
import info.movito.themoviedbapi.model.movies.Crew;
import info.movito.themoviedbapi.model.movies.MovieDb;
import info.movito.themoviedbapi.model.tv.series.CreatedBy;
import info.movito.themoviedbapi.model.tv.series.TvSeriesDb;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.cyberrealm.tech.muvio.mapper.ActorMapper;
import org.cyberrealm.tech.muvio.mapper.MediaMapper;
import org.cyberrealm.tech.muvio.mapper.ReviewMapper;
import org.cyberrealm.tech.muvio.model.Actor;
import org.cyberrealm.tech.muvio.model.Category;
import org.cyberrealm.tech.muvio.model.LocalizationEntry;
import org.cyberrealm.tech.muvio.model.LocalizationMedia;
import org.cyberrealm.tech.muvio.model.Media;
import org.cyberrealm.tech.muvio.model.Review;
import org.cyberrealm.tech.muvio.model.RoleActor;
import org.cyberrealm.tech.muvio.model.TopLists;
import org.cyberrealm.tech.muvio.model.Vibe;
import org.cyberrealm.tech.muvio.service.CategoryService;
import org.cyberrealm.tech.muvio.service.LocalizationMediaFactory;
import org.cyberrealm.tech.muvio.service.MediaFactory;
import org.cyberrealm.tech.muvio.service.TmDbService;
import org.cyberrealm.tech.muvio.service.TopListService;
import org.cyberrealm.tech.muvio.service.VibeService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MediaFactoryImpl implements MediaFactory {
    private static final String PRODUCER = "Producer";
    private static final String EXECUTIVE_PRODUCER = "Executive Producer";
    private static final String SERIES_DIRECTOR = "Series Director";
    private static final String HOST = "host";
    private static final String CREATOR = "Creator";
    private static final String CHIEF_DIRECTOR = "Chief Director";
    private static final String EPISODE_DIRECTOR = "Episode Director";
    private static final String DEFAULT_LANGUAGE = "null";
    private static final int MAX_NUMBER_OF_ACTORS = 3;
    private final TmDbService tmdbService;
    private final CategoryService categoryService;
    private final VibeService vibeService;
    private final MediaMapper mediaMapper;
    private final ActorMapper actorMapper;
    private final ReviewMapper reviewMapper;
    private final TopListService topListService;
    private final Set<LocalizationEntry> localizationEntrySet;
    private final LocalizationMediaFactory localizationMediaFactory;

    @Override
    public Media createMovie(Integer movieId, Set<String> moviesTop250,
                             Set<String> oscarWinningMedia, Map<Integer, Actor> actors,
                             Set<LocalizationMedia> localizationMediaStorage) {
        final MovieDb movieDb = tmdbService.fetchMovieDetails(movieId, LANGUAGE_EN);
        final List<Keyword> keywords = tmdbService.fetchMovieKeywords(movieId)
                .getKeywords();
        final Credits credits = tmdbService.fetchMovieCredits(movieId, LANGUAGE_EN);
        final List<Crew> crew = credits.getCrew();
        final Media media = mediaMapper.toEntity(movieDb);
        final Set<String> photoPaths = tmdbService.fetchMoviePhotos(DEFAULT_LANGUAGE, movieId,
                media.getPosterPath());
        final String movieDirector = getMovieDirector(crew);
        if (isInvalidMedia(movieDirector, photoPaths, movieId, localizationMediaStorage,
                localizationMediaFactory::createFromMovie)) {
            return null;
        }
        final Double voteAverage = media.getRating();
        final Integer voteCount = movieDb.getVoteCount();
        final Double popularity = movieDb.getPopularity();
        final String title = media.getTitle();
        fillCommonMediaInfo(
                media,
                tmdbService.fetchMovieTrailer(movieId, LANGUAGE_EN),
                photoPaths,
                getMovieActors(credits.getCast(), actors),
                getReviews(() -> tmdbService.fetchMovieReviews(LANGUAGE_EN, movieId)),
                movieDirector,
                vibeService.getVibes(tmdbService.fetchTmDbMovieRatings(movieId),
                        media.getGenres()),
                categoryService.putCategories(media.getOverview().toLowerCase(),
                        keywords, voteAverage, voteCount, popularity, moviesTop250, title),
                topListService.putTopLists(keywords, voteAverage, voteCount, popularity,
                        media.getReleaseYear(), oscarWinningMedia, title, movieDb.getBudget(),
                        movieDb.getRevenue())
        );
        return media;
    }

    @Override
    public Media createTvSerial(Integer seriesId, Set<String> serialsTop250,
                                Set<String> emmyWinningMedia, Map<Integer, Actor> actors,
                                Set<LocalizationMedia> localizationMediaStorage) {
        final List<Keyword> keywords = tmdbService.fetchTvSerialsKeywords(seriesId)
                .getResults();
        final TvSeriesDb tvSeriesDb = tmdbService.fetchTvSerialsDetails(seriesId, LANGUAGE_EN);
        final Media media = mediaMapper.toEntity(tvSeriesDb);
        final info.movito.themoviedbapi.model.tv.core.credits.Credits credits
                = tmdbService.fetchTvSerialsCredits(seriesId, LANGUAGE_EN);
        final List<info.movito.themoviedbapi.model.tv.core.credits.Cast> cast = credits.getCast();
        final String tvDirector = getTvDirector(
                tvSeriesDb.getCreatedBy(), cast, credits.getCrew());
        final Set<String> photoPaths = tmdbService.fetchTvSerialsPhotos(DEFAULT_LANGUAGE, seriesId,
                media.getPosterPath());
        if (isInvalidMedia(tvDirector, photoPaths, seriesId, localizationMediaStorage,
                localizationMediaFactory::createFromSerial)) {
            return null;
        }
        final Double voteAverage = media.getRating();
        final Integer voteCount = tvSeriesDb.getVoteCount();
        final Double popularity = tvSeriesDb.getPopularity();
        final String title = media.getTitle();
        fillCommonMediaInfo(
                media,
                tmdbService.fetchTvSerialsTrailer(seriesId, LANGUAGE_EN),
                photoPaths,
                getTvActors(cast, actors),
                getReviews(() -> tmdbService.fetchTvSerialsReviews(LANGUAGE_EN, seriesId)),
                tvDirector,
                vibeService.getVibes(tmdbService.fetchTmDbTvRatings(seriesId),
                        media.getGenres()),
                categoryService.putCategories(media.getOverview().toLowerCase(),
                        keywords, voteAverage, voteCount, popularity, serialsTop250, title),
                topListService.putTopListsForTvShow(keywords, voteAverage, voteCount,
                        popularity, media.getReleaseYear(), emmyWinningMedia, title)
        );
        return media;
    }

    private String getMovieDirector(List<Crew> crews) {
        return tryGetFirstNonNull(
                () -> findCrewMemberByJob(crews, DIRECTOR),
                () -> findCrewMemberByJob(crews, PRODUCER)
        );
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

    private String findCrewMemberByJob(List<Crew> crews, String job) {
        return crews.stream().filter(crew -> crew.getJob().equalsIgnoreCase(job))
                .map(Crew::getName).findFirst().orElse(null);
    }

    private String getTvDirector(List<CreatedBy> creators,
                                 List<info.movito.themoviedbapi.model.tv.core.credits.Cast> casts,
                                 List<info.movito.themoviedbapi.model.tv.core.credits.Crew> crews
    ) {
        return tryGetFirstNonNull(
                () -> creators.stream().map(NamedIdElement::getName).findFirst().orElse(null),
                () -> getCrewNameByJob(crews, DIRECTOR),
                () -> getCrewNameByJob(crews, CREATOR),
                () -> getCrewNameByJob(crews, PRODUCER),
                () -> getCrewNameByJob(crews, EXECUTIVE_PRODUCER),
                () -> getCrewNameByJob(crews, SERIES_DIRECTOR),
                () -> getCrewNameByJob(crews, EPISODE_DIRECTOR),
                () -> getCrewNameByJob(crews, CHIEF_DIRECTOR),
                () -> casts.stream().filter(cast -> cast.getCharacter() != null
                                && cast.getCharacter().toLowerCase().contains(HOST))
                        .findFirst().map(NamedIdElement::getName).orElse(null));
    }

    @SafeVarargs
    private String tryGetFirstNonNull(Supplier<String>... suppliers) {
        for (Supplier<String> supplier : suppliers) {
            String result = supplier.get();
            if (result != null && !result.isEmpty()) {
                return result;
            }
        }
        return null;
    }

    private String getCrewNameByJob(
            List<info.movito.themoviedbapi.model.tv.core.credits.Crew> crews, String job) {
        return crews.stream().filter(crew -> crew.getJob() != null
                        && crew.getJob().equalsIgnoreCase(job)).findFirst()
                .map(NamedIdElement::getName).orElse(null);
    }

    private boolean isInvalidMedia(
            String director, Set<String> photoPaths, Integer id,
            Set<LocalizationMedia> localizationMediaStorage,
            BiFunction<Integer, Set<LocalizationMedia>, Boolean> localizationCreator) {
        if (director == null || photoPaths.isEmpty()) {
            return true;
        }
        return !localizationEntrySet.isEmpty()
                && !localizationCreator.apply(id, localizationMediaStorage);
    }

    private void fillCommonMediaInfo(Media media, String trailer, Set<String> photoPaths,
                                     List<RoleActor> actors, List<Review> reviews,
                                     String director, Set<Vibe> vibes, Set<Category> categories,
                                     Set<TopLists> topLists) {
        media.setTrailer(trailer);
        media.setPhotos(photoPaths);
        media.setActors(actors);
        media.setReviews(reviews);
        media.setDirector(director);
        media.setVibes(vibes);
        media.setCategories(categories);
        media.setTopLists(topLists);
    }
}
