package org.cyberrealm.tech.muvio.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cyberrealm.tech.muvio.common.Constants.DIRECTOR;
import static org.cyberrealm.tech.muvio.common.Constants.ONE;
import static org.cyberrealm.tech.muvio.common.Constants.THREE;
import static org.cyberrealm.tech.muvio.common.Constants.TWO;
import static org.cyberrealm.tech.muvio.util.TestConstants.AUTHOR;
import static org.cyberrealm.tech.muvio.util.TestConstants.CONTENT_STRING;
import static org.cyberrealm.tech.muvio.util.TestConstants.DIRECTOR_NAME;
import static org.cyberrealm.tech.muvio.util.TestConstants.ID_STRING;
import static org.cyberrealm.tech.muvio.util.TestConstants.OVERVIEW;
import static org.cyberrealm.tech.muvio.util.TestConstants.PATH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import info.movito.themoviedbapi.model.core.image.Artwork;
import info.movito.themoviedbapi.model.movies.Cast;
import info.movito.themoviedbapi.model.movies.Credits;
import info.movito.themoviedbapi.model.movies.Crew;
import info.movito.themoviedbapi.model.movies.Images;
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
import org.cyberrealm.tech.muvio.model.LocalizationEntry;
import org.cyberrealm.tech.muvio.model.Media;
import org.cyberrealm.tech.muvio.model.Review;
import org.cyberrealm.tech.muvio.service.CategoryService;
import org.cyberrealm.tech.muvio.service.LocalizationMediaFactory;
import org.cyberrealm.tech.muvio.service.TmDbService;
import org.cyberrealm.tech.muvio.service.TopListService;
import org.cyberrealm.tech.muvio.service.VibeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MediaFactoryImplTest {
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
    @Mock
    private Set<LocalizationEntry> localizationEntrySet;
    @Mock
    private LocalizationMediaFactory localizationMediaFactory;
    @InjectMocks
    private MediaFactoryImpl mediaFactory;

    @Test
    @DisplayName("Verify createMovie() method works")
    public void createMovie_ValidResponse_ReturnMedia() {
        final Media media = getMedia();
        final Actor actor = getActor();
        final List<info.movito.themoviedbapi.model.core.Review> tmDbReviews = getTmDbReviews();
        final MovieDb movieDb = getMovieDb();

        when(tmdbService.fetchFullMovieDetails(anyInt(), anyString())).thenReturn(movieDb);
        when(mediaMapper.toEntity(any(MovieDb.class))).thenReturn(media);
        when(tmdbService.extractPhotos(any(), anyString())).thenReturn(Set.of(PATH));
        when(actorMapper.toActorEntity(any(Cast.class))).thenReturn(actor);
        when(vibeService.getVibes(any(), any())).thenReturn(Set.of());
        when(categoryService.putCategories(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Set.of());
        when(topListService.putTopLists(any(), any(), any(), any(), any(), any(), any(), any(),
                any())).thenReturn(Set.of());
        when(tmdbService.extractReviews(any())).thenReturn(tmDbReviews);
        when(reviewMapper.toEntity(any())).thenReturn(getReview());
        when(localizationEntrySet.isEmpty()).thenReturn(true);

        final Map<Integer, Actor> actors = new ConcurrentHashMap<>();
        assertThat(mediaFactory.createMovie(THREE, Set.of(), Set.of(), actors, Set.of()))
                .isEqualTo(media);
        assertThat(actors.get(TWO)).isEqualTo(actor);
    }

    @Test
    @DisplayName("Verify createTvSerial() method works")
    public void createTvSerial_ValidResponse_ReturnMedia() {
        final Media media = getMedia();
        final Actor actor = getActor();
        final List<info.movito.themoviedbapi.model.core.Review> tmDbReviews = getTmDbReviews();
        final TvSeriesDb tvSeriesDb = getTvSerial();

        when(tmdbService.fetchFullTvSeriesDetails(anyInt(), anyString())).thenReturn(tvSeriesDb);
        when(mediaMapper.toEntity(any(TvSeriesDb.class))).thenReturn(media);
        when(tmdbService.extractPhotos(any(), anyString())).thenReturn(Set.of(PATH));
        when(actorMapper.toActorEntity(any(
                info.movito.themoviedbapi.model.tv.core.credits.Cast.class))).thenReturn(actor);
        when(vibeService.getVibes(any(), any())).thenReturn(Set.of());
        when(categoryService.putCategories(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Set.of());
        when(topListService.putTopListsForTvShow(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Set.of());
        when(tmdbService.extractReviews(any())).thenReturn(tmDbReviews);
        when(reviewMapper.toEntity(any())).thenReturn(getReview());
        when(localizationEntrySet.isEmpty()).thenReturn(true);

        final Map<Integer, Actor> actors = new ConcurrentHashMap<>();
        assertThat(mediaFactory.createTvSerial(THREE, Set.of(), Set.of(), actors, Set.of()))
                .isEqualTo(media);
        assertThat(actors.get(TWO)).isEqualTo(actor);
    }

    private MovieDb getMovieDb() {
        final MovieDb movieDb = new MovieDb();
        movieDb.setId(THREE);
        movieDb.setOverview(OVERVIEW);
        final Credits credits = new Credits();
        final Cast cast = new Cast();
        cast.setId(TWO);
        credits.setCast(List.of(cast));
        final Crew crew = new Crew();
        crew.setJob(DIRECTOR);
        crew.setName(DIRECTOR_NAME);
        credits.setCrew(List.of(crew));
        movieDb.setCredits(credits);

        final Images images = new Images();
        final Artwork artwork = new Artwork();
        artwork.setFilePath(PATH);
        images.setBackdrops(List.of(artwork));
        movieDb.setImages(images);

        return movieDb;
    }

    private TvSeriesDb getTvSerial() {
        final TvSeriesDb tvSeriesDb = new TvSeriesDb();
        tvSeriesDb.setId(THREE);
        tvSeriesDb.setOverview(OVERVIEW);
        final CreatedBy createdBy = new CreatedBy();
        createdBy.setName(DIRECTOR_NAME);
        createdBy.setId(ONE);
        tvSeriesDb.setCreatedBy(List.of(createdBy));

        final info.movito.themoviedbapi.model.tv.core.credits.Credits credits =
                new info.movito.themoviedbapi.model.tv.core.credits.Credits();
        final info.movito.themoviedbapi.model.tv.core.credits.Cast cast =
                new info.movito.themoviedbapi.model.tv.core.credits.Cast();
        cast.setId(TWO);
        credits.setCast(List.of(cast));
        tvSeriesDb.setCredits(credits);

        final info.movito.themoviedbapi.model.tv.series.Images images =
                new info.movito.themoviedbapi.model.tv.series.Images();
        final Artwork artwork = new Artwork();
        artwork.setFilePath(PATH);
        images.setBackdrops(List.of(artwork));
        tvSeriesDb.setImages(images);

        return tvSeriesDb;
    }

    private List<info.movito.themoviedbapi.model.core.Review> getTmDbReviews() {
        final info.movito.themoviedbapi.model.core.Review review
                = new info.movito.themoviedbapi.model.core.Review();
        review.setAuthor(AUTHOR);
        review.setId(ID_STRING);
        review.setContent(CONTENT_STRING);
        return List.of(review);
    }

    private Review getReview() {
        final Review review = new Review();
        review.setAuthor(AUTHOR);
        review.setId(ID_STRING);
        review.setContent(CONTENT_STRING);
        return review;
    }

    private Media getMedia() {
        final Media media = new Media();
        media.setId(String.valueOf(THREE));
        media.setOverview(OVERVIEW);
        media.setPhotos(Set.of(PATH));
        media.setPosterPath(PATH);
        return media;
    }

    private Actor getActor() {
        final Actor actor = new Actor();
        actor.setId(TWO);
        return actor;
    }
}
