package org.cyberrealm.tech.muvio.service.impl;

import static org.cyberrealm.tech.muvio.common.Constants.DIRECTOR;

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
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.cyberrealm.tech.muvio.mapper.ActorMapper;
import org.cyberrealm.tech.muvio.mapper.MediaMapper;
import org.cyberrealm.tech.muvio.mapper.ReviewMapper;
import org.cyberrealm.tech.muvio.model.Actor;
import org.cyberrealm.tech.muvio.model.Media;
import org.cyberrealm.tech.muvio.model.Review;
import org.cyberrealm.tech.muvio.model.RoleActor;
import org.cyberrealm.tech.muvio.service.CategoryService;
import org.cyberrealm.tech.muvio.service.MediaFactory;
import org.cyberrealm.tech.muvio.service.TmDbService;
import org.cyberrealm.tech.muvio.service.TopListService;
import org.cyberrealm.tech.muvio.service.VibeService;
import org.springframework.stereotype.Component;

@Component
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

    @Override
    public Media createMovie(String language, Integer movieId, Set<String> moviesTop250,
                             Set<String> oscarWinningMedia, Map<Integer, Actor> actors) {
        final MovieDb movieDb = tmdbService.fetchMovieDetails(movieId, language);
        final List<Keyword> keywords = tmdbService.fetchMovieKeywords(movieId)
                .getKeywords();
        final Credits credits = tmdbService.fetchMovieCredits(movieId, language);
        final List<Crew> crew = credits.getCrew();
        if (crew.isEmpty()) {
            return null;
        }
        final Media media = mediaMapper.toEntity(movieDb);
        final Double voteAverage = media.getRating();
        final Integer voteCount = movieDb.getVoteCount();
        final Double popularity = movieDb.getPopularity();
        final String title = media.getTitle();
        media.setTrailer(tmdbService.fetchMovieTrailer(movieId, language));
        media.setPhotos(tmdbService.fetchMoviePhotos(DEFAULT_LANGUAGE, movieId,
                media.getPosterPath()));
        media.setDirector(getMovieDirector(crew));
        media.setActors(getMovieActors(credits.getCast(), actors));
        media.setReviews(getReviews(() ->
                tmdbService.fetchMovieReviews(language, movieId)));
        media.setVibes(vibeService.getVibes(tmdbService.fetchTmDbMovieRatings(movieId),
                media.getGenres()));
        media.setCategories(categoryService.putCategories(media.getOverview().toLowerCase(),
                keywords, voteAverage, voteCount, popularity, moviesTop250, title));
        media.setTopLists(topListService.putTopLists(keywords, voteAverage, voteCount, popularity,
                media.getReleaseYear(), oscarWinningMedia, title, movieDb.getBudget(),
                movieDb.getRevenue()));
        return media;
    }

    @Override
    public Media createTvSerial(String language, Integer seriesId, Set<String> serialsTop250,
                                Set<String> emmyWinningMedia, Map<Integer, Actor> actors) {
        final List<Keyword> keywords = tmdbService.fetchTvSerialsKeywords(seriesId)
                .getResults();
        final TvSeriesDb tvSeriesDb = tmdbService.fetchTvSerialsDetails(seriesId, language);
        final Media media = mediaMapper.toEntity(tvSeriesDb);
        final info.movito.themoviedbapi.model.tv.core.credits.Credits credits
                = tmdbService.fetchTvSerialsCredits(seriesId, language);
        final List<info.movito.themoviedbapi.model.tv.core.credits.Cast> cast = credits.getCast();
        final String tvDirector = getTvDirector(
                tvSeriesDb.getCreatedBy(), cast, credits.getCrew());
        if (tvDirector == null) {
            return null;
        }
        final Double voteAverage = media.getRating();
        final Integer voteCount = tvSeriesDb.getVoteCount();
        final Double popularity = tvSeriesDb.getPopularity();
        final String title = media.getTitle();
        media.setTrailer(tmdbService.fetchTvSerialsTrailer(seriesId, language));
        media.setPhotos(tmdbService.fetchTvSerialsPhotos(DEFAULT_LANGUAGE, seriesId,
                media.getPosterPath()));
        media.setDirector(tvDirector);
        media.setActors(getTvActors(cast, actors));
        media.setReviews(getReviews(() ->
                tmdbService.fetchTvSerialsReviews(language, seriesId)));
        media.setCategories(categoryService.putCategories(media.getOverview().toLowerCase(),
                keywords, voteAverage, voteCount, popularity, serialsTop250, title));
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
        return tryGetFirstNonNull(
                () -> findCrewMemberByJob(crews, DIRECTOR),
                () -> findCrewMemberByJob(crews, PRODUCER)
        );
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
}
