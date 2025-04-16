package org.cyberrealm.tech.muvio.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cyberrealm.tech.muvio.common.Constants.AUTHOR;
import static org.cyberrealm.tech.muvio.common.Constants.CONTENT;
import static org.cyberrealm.tech.muvio.common.Constants.DIRECTOR;
import static org.cyberrealm.tech.muvio.common.Constants.ID_STRING;
import static org.cyberrealm.tech.muvio.common.Constants.LANGUAGE_EN;
import static org.cyberrealm.tech.muvio.common.Constants.ONE;
import static org.cyberrealm.tech.muvio.common.Constants.OVERVIEW;
import static org.cyberrealm.tech.muvio.common.Constants.THREE;
import static org.cyberrealm.tech.muvio.common.Constants.TWO;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import info.movito.themoviedbapi.model.core.TvKeywords;
import info.movito.themoviedbapi.model.movies.Cast;
import info.movito.themoviedbapi.model.movies.Credits;
import info.movito.themoviedbapi.model.movies.Crew;
import info.movito.themoviedbapi.model.movies.KeywordResults;
import info.movito.themoviedbapi.model.movies.MovieDb;
import info.movito.themoviedbapi.model.tv.series.CreatedBy;
import info.movito.themoviedbapi.model.tv.series.TvSeriesDb;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.cyberrealm.tech.muvio.mapper.ActorMapper;
import org.cyberrealm.tech.muvio.mapper.MediaMapper;
import org.cyberrealm.tech.muvio.mapper.ReviewMapper;
import org.cyberrealm.tech.muvio.model.Actor;
import org.cyberrealm.tech.muvio.model.Media;
import org.cyberrealm.tech.muvio.model.Review;
import org.cyberrealm.tech.muvio.service.impl.MediaFactoryImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MediaFactoryTest {
    private static final String ACTOR_NAME = "ActorName";
    private static final String DIRECTOR_NAME = "directorName";

    @Mock
    private TmDbService tmdbService;
    @Mock
    private CategoryService categoryService;
    @Mock
    private VibeService vibeService;
    @Mock
    private MediaMapper mediaMapper;
    @Mock
    private ActorMapper actorMapper;
    @Mock
    private ReviewMapper reviewMapper;
    @Mock
    private TopListService topListService;
    @InjectMocks
    private MediaFactoryImpl mediaFactory;

    @Test
    @DisplayName("Verify createMovie() method works")
    public void createMovie_ValidResponse_ReturnMedia() {
        final Media media = getMedia();
        final Actor actor = getActor();
        List<info.movito.themoviedbapi.model.core.Review> tmDbReviews = getTmDbReviews();
        when(tmdbService.fetchMovieKeywords(anyInt())).thenReturn(new KeywordResults());
        when(tmdbService.fetchMovieDetails(anyInt(), anyString())).thenReturn(new MovieDb());
        when(mediaMapper.toEntity(any(MovieDb.class))).thenReturn(media);
        when(tmdbService.fetchMovieCredits(anyInt(), anyString())).thenReturn(getCredits());
        when(actorMapper.toActorEntity(any(Cast.class))).thenReturn(actor);
        when(vibeService.getVibes(any(), any())).thenReturn(Set.of());
        when(categoryService.putCategories(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Set.of());
        when(topListService.putTopLists(any(), any(), any(), any(), any(), any(), any(), any(),
                any())).thenReturn(Set.of());
        when(tmdbService.fetchMovieReviews(any(), anyInt())).thenReturn(tmDbReviews);
        when(reviewMapper.toEntity(any())).thenReturn(getReview());
        final Map<Integer, Actor> actors = new ConcurrentHashMap<>();
        assertThat(mediaFactory.createMovie(LANGUAGE_EN, THREE, Set.of(), Set.of(), actors))
                .isEqualTo(media);
        assertThat(actors.get(TWO)).isEqualTo(actor);
    }

    @Test
    @DisplayName("Verify createTvSerial() method works")
    public void createTvSerial_ValidResponse_ReturnMedia() {
        final Media media = getMedia();
        final Actor actor = getActor();
        List<info.movito.themoviedbapi.model.core.Review> tmDbReviews = getTmDbReviews();
        when(tmdbService.fetchTvSerialsKeywords(anyInt())).thenReturn(new TvKeywords());
        when(tmdbService.fetchTvSerialsDetails(anyInt(), anyString())).thenReturn(getTvSerial());
        when(mediaMapper.toEntity(any(TvSeriesDb.class))).thenReturn(media);
        when(tmdbService.fetchTvSerialsCredits(anyInt(), anyString())).thenReturn(getTvCredits());
        when(actorMapper.toActorEntity(any(
                info.movito.themoviedbapi.model.tv.core.credits.Cast.class))).thenReturn(actor);
        when(vibeService.getVibes(any(), any())).thenReturn(Set.of());
        when(categoryService.putCategories(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Set.of());
        when(topListService.putTopListsForTvShow(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Set.of());
        when(tmdbService.fetchTvSerialsReviews(any(), anyInt())).thenReturn(tmDbReviews);
        when(reviewMapper.toEntity(any())).thenReturn(getReview());
        final Map<Integer, Actor> actors = new ConcurrentHashMap<>();
        assertThat(mediaFactory.createTvSerial(LANGUAGE_EN, THREE, Set.of(), Set.of(), actors))
                .isEqualTo(media);
        assertThat(actors.get(TWO)).isEqualTo(actor);
    }

    private TvSeriesDb getTvSerial() {
        final TvSeriesDb tvSeriesDb = new TvSeriesDb();
        tvSeriesDb.setId(THREE);
        tvSeriesDb.setOverview(OVERVIEW);
        final CreatedBy createdBy = new CreatedBy();
        createdBy.setName(DIRECTOR_NAME);
        createdBy.setId(ONE);
        tvSeriesDb.setCreatedBy(List.of(createdBy));
        return tvSeriesDb;
    }

    private info.movito.themoviedbapi.model.tv.core.credits.Credits getTvCredits() {
        final info.movito.themoviedbapi.model.tv.core.credits.Credits credits
                = new info.movito.themoviedbapi.model.tv.core.credits.Credits();
        final info.movito.themoviedbapi.model.tv.core.credits.Cast cast
                = new info.movito.themoviedbapi.model.tv.core.credits.Cast();
        cast.setName(ACTOR_NAME);
        cast.setId(TWO);
        credits.setCast(List.of(cast));
        return credits;
    }

    private List<info.movito.themoviedbapi.model.core.Review> getTmDbReviews() {
        final info.movito.themoviedbapi.model.core.Review review
                = new info.movito.themoviedbapi.model.core.Review();
        review.setAuthor(AUTHOR);
        review.setId(ID_STRING);
        review.setContent(CONTENT);
        return List.of(review);
    }

    private Review getReview() {
        final Review review = new Review();
        review.setAuthor(AUTHOR);
        review.setId(ID_STRING);
        review.setContent(CONTENT);
        return review;
    }

    private Media getMedia() {
        final Media media = new Media();
        media.setId(String.valueOf(THREE));
        media.setOverview(OVERVIEW);
        return media;
    }

    private Credits getCredits() {
        final Credits credits = new Credits();
        final Cast cast = new Cast();
        cast.setName(ACTOR_NAME);
        cast.setId(TWO);
        credits.setCast(List.of(cast));
        final Crew crew = new Crew();
        crew.setJob(DIRECTOR);
        crew.setName(DIRECTOR_NAME);
        credits.setCrew(List.of(crew));
        return credits;
    }

    private Actor getActor() {
        final Actor actor = new Actor();
        actor.setName(ACTOR_NAME);
        actor.setId(TWO);
        return actor;
    }
}
