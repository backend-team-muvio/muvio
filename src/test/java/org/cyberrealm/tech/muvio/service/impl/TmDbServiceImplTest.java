package org.cyberrealm.tech.muvio.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cyberrealm.tech.muvio.common.Constants.IMAGE_PATH_W200;
import static org.cyberrealm.tech.muvio.common.Constants.LANGUAGE_EN;
import static org.cyberrealm.tech.muvio.common.Constants.MIN_VOTE_COUNT;
import static org.cyberrealm.tech.muvio.common.Constants.ONE;
import static org.cyberrealm.tech.muvio.common.Constants.REGION_US;
import static org.cyberrealm.tech.muvio.common.Constants.TITLE;
import static org.cyberrealm.tech.muvio.common.Constants.TRAILER;
import static org.cyberrealm.tech.muvio.common.Constants.TWO;
import static org.cyberrealm.tech.muvio.common.Constants.YOUTUBE_PATH;
import static org.cyberrealm.tech.muvio.common.Constants.ZERO;
import static org.cyberrealm.tech.muvio.util.TestConstants.AUTHOR;
import static org.cyberrealm.tech.muvio.util.TestConstants.CONTENT_STRING;
import static org.cyberrealm.tech.muvio.util.TestConstants.ID_STRING;
import static org.cyberrealm.tech.muvio.util.TestConstants.OVERVIEW;
import static org.cyberrealm.tech.muvio.util.TestConstants.PATH;
import static org.cyberrealm.tech.muvio.util.TestConstants.POSTER_PATH;
import static org.cyberrealm.tech.muvio.util.TestConstants.VOTE_AVERAGE_8;
import static org.cyberrealm.tech.muvio.util.TestConstants.YEAR_2020;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import info.movito.themoviedbapi.TmdbDiscover;
import info.movito.themoviedbapi.TmdbMovieLists;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.TmdbSearch;
import info.movito.themoviedbapi.TmdbTvSeries;
import info.movito.themoviedbapi.TmdbTvSeriesLists;
import info.movito.themoviedbapi.model.core.Movie;
import info.movito.themoviedbapi.model.core.MovieResultsPage;
import info.movito.themoviedbapi.model.core.Review;
import info.movito.themoviedbapi.model.core.ReviewResultsPage;
import info.movito.themoviedbapi.model.core.TvSeries;
import info.movito.themoviedbapi.model.core.TvSeriesResultsPage;
import info.movito.themoviedbapi.model.core.image.Artwork;
import info.movito.themoviedbapi.model.core.video.Video;
import info.movito.themoviedbapi.model.core.video.VideoResults;
import info.movito.themoviedbapi.model.movies.MovieDb;
import info.movito.themoviedbapi.model.movies.ReleaseDate;
import info.movito.themoviedbapi.model.movies.ReleaseDateResults;
import info.movito.themoviedbapi.model.movies.ReleaseInfo;
import info.movito.themoviedbapi.model.reviews.AuthorDetails;
import info.movito.themoviedbapi.model.tv.series.ContentRating;
import info.movito.themoviedbapi.model.tv.series.ContentRatingResults;
import info.movito.themoviedbapi.model.tv.series.TvSeriesDb;
import info.movito.themoviedbapi.tools.appendtoresponse.MovieAppendToResponse;
import info.movito.themoviedbapi.tools.appendtoresponse.TvSeriesAppendToResponse;
import info.movito.themoviedbapi.tools.builders.discover.DiscoverMovieParamBuilder;
import info.movito.themoviedbapi.tools.builders.discover.DiscoverTvParamBuilder;
import info.movito.themoviedbapi.tools.sortby.DiscoverMovieSortBy;
import info.movito.themoviedbapi.tools.sortby.DiscoverTvSortBy;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.SneakyThrows;
import org.cyberrealm.tech.muvio.service.ImageSimilarityService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TmDbServiceImplTest {
    private static final String RELEASE_DATE = "2020.11.11";
    private static final String AGE_RATING_G = "G";

    @Mock
    private TmdbMovies tmdbMovies;
    @Mock
    private TmdbMovieLists tmdbMovieLists;
    @Mock
    private TmdbTvSeriesLists tmdbTvSeriesLists;
    @Mock
    private TmdbTvSeries tmdbTvSeries;
    @Mock
    private TmdbSearch tmdbSearch;
    @Mock
    private TmdbDiscover tmdbDiscover;
    @Mock
    private DiscoverMovieParamBuilder discoverMovieParamBuilder;
    @Mock
    private DiscoverTvParamBuilder discoverTvParamBuilder;
    @Mock
    private ImageSimilarityService imageSimilarityService;
    @InjectMocks
    private TmDbServiceImpl tmDbService;

    @SneakyThrows
    @Test
    @DisplayName("Verify fetchPopularMovies() method works")
    public void fetchPopularMovies_ValidResponse_ReturnSetIds() {
        when(tmdbMovieLists.getPopular(anyString(), anyInt(), anyString()))
                .thenReturn(getMovieResultsPage());
        assertThat(tmDbService.fetchPopularMovies(LANGUAGE_EN, ONE, REGION_US))
                .isEqualTo(Set.of(ONE));
    }

    @SneakyThrows
    @Test
    @DisplayName("Verify fetchFullMovieDetails() method works")
    public void fetchFullMovieDetails_ValidResponse_ReturnMovieDb() {
        final MovieDb movieDb = getMovieDb();
        when(tmdbMovies.getDetails(anyInt(), anyString(), any(MovieAppendToResponse[].class)))
                .thenReturn(movieDb);
        assertThat(tmDbService.fetchFullMovieDetails(ONE, LANGUAGE_EN)).isEqualTo(movieDb);
    }

    @SneakyThrows
    @Test
    @DisplayName("Verify fetchPopularTvSerials() method works")
    public void fetchPopularTvSerials_ValidResponse_ReturnSetIds() {
        when(tmdbTvSeriesLists.getPopular(anyString(), anyInt()))
                .thenReturn(getTvSeriesResultsPage());
        assertThat(tmDbService.fetchPopularTvSerials(LANGUAGE_EN, ONE)).isEqualTo(Set.of(TWO));
    }

    @SneakyThrows
    @Test
    @DisplayName("Verify fetchFullTvSeriesDetails() method works")
    public void fetchFullTvSeriesDetails_ValidResponse_ReturnTvSeriesDb() {
        final TvSeriesDb tvSeriesDb = getTvSeriesDb();
        when(tmdbTvSeries.getDetails(anyInt(), anyString(), any(TvSeriesAppendToResponse[].class)))
                .thenReturn(tvSeriesDb);
        assertThat(tmDbService.fetchFullTvSeriesDetails(TWO, LANGUAGE_EN)).isEqualTo(tvSeriesDb);
    }

    @Test
    @DisplayName("Verify extractTrailer() method works")
    public void extractTrailer_ValidResponse_ReturnPath() {
        assertThat(tmDbService.extractTrailer(getVideoResults()))
                .isEqualTo(YOUTUBE_PATH + PATH);
    }

    @Test
    @DisplayName("Verify extractPhotos() method works")
    public void extractPhotos_ValidResponse_ReturnSetPaths() {
        doAnswer(invocation -> {
            final String imageUrl = invocation.getArgument(ZERO);
            final Set<String> filePaths = invocation.getArgument(TWO);
            filePaths.add(imageUrl);
            return null;
        }).when(imageSimilarityService).addIfUniqueHash(anyString(), anySet(), anySet());

        assertThat(tmDbService.extractPhotos(getListArtworks(), POSTER_PATH))
                .isEqualTo(Set.of(IMAGE_PATH_W200 + PATH));
    }

    @Test
    @DisplayName("Verify extractReviews() method works")
    public void extractReviews_ValidResponse_ReturnListReviews() {
        final List<Review> expectedReviews = getListReviews();
        expectedReviews.get(ZERO).getAuthorDetails().setAvatarPath(IMAGE_PATH_W200 + PATH);

        assertThat(tmDbService.extractReviews(getReviewResultsPage()))
                .isEqualTo(expectedReviews);
    }

    @Test
    @DisplayName("Verify extractMovieRatings() method works")
    public void extractMovieRatings_ValidResponse_ReturnSetAgeRatings() {
        final MovieDb movieDb = getMovieDb();
        movieDb.setReleaseDates(getReleaseDates());
        assertThat(tmDbService.extractMovieRatings(movieDb)).isEqualTo(Set.of(AGE_RATING_G));
    }

    @Test
    @DisplayName("Verify extractTvRatings() method works")
    public void extractTvRatings_ValidResponse_ReturnSetAgeRatings() {
        final TvSeriesDb tvSeriesDb = getTvSeriesDb();
        tvSeriesDb.setContentRatings(getRatingResults());
        assertThat(tmDbService.extractTvRatings(tvSeriesDb)).isEqualTo(Set.of(AGE_RATING_G));
    }

    @SneakyThrows
    @Test
    @DisplayName("Verify searchMovies() method works")
    public void searchMovies_ValidResponse_ReturnOptionalOfId() {
        when(tmdbSearch.searchMovie(TITLE, false, LANGUAGE_EN, null, ONE,
                REGION_US, null)).thenReturn(getMovieResultsPage());
        assertThat(tmDbService.searchMovies(TITLE, LANGUAGE_EN, REGION_US))
                .isEqualTo(Optional.of(ONE));
    }

    @SneakyThrows
    @Test
    @DisplayName("Verify searchTvSeries() method works")
    public void searchTvSeries_ValidResponse_ReturnOptionalOfId() {
        when(tmdbSearch.searchTv(TITLE, null, false,
                LANGUAGE_EN, ONE, null)).thenReturn(getTvSeriesResultsPage());
        assertThat(tmDbService.searchTvSeries(TITLE, LANGUAGE_EN)).isEqualTo(Optional.of(TWO));
    }

    @SneakyThrows
    @Test
    @DisplayName("Verify getFilteredMovies() method works")
    public void getFilteredMovies_ValidResponse_ReturnSetIds() {
        when(discoverMovieParamBuilder.year(anyInt())).thenReturn(discoverMovieParamBuilder);
        when(discoverMovieParamBuilder.withoutGenres(anyList()))
                .thenReturn(discoverMovieParamBuilder);
        when(discoverMovieParamBuilder.voteAverageGte(anyDouble()))
                .thenReturn(discoverMovieParamBuilder);
        when(discoverMovieParamBuilder.voteCountGte(anyDouble()))
                .thenReturn(discoverMovieParamBuilder);
        when(discoverMovieParamBuilder.page(anyInt())).thenReturn(discoverMovieParamBuilder);
        when(tmdbDiscover.getMovie(discoverMovieParamBuilder
                .year(anyInt())
                .withoutGenres(anyList())
                .voteAverageGte(anyDouble()).voteCountGte(anyDouble()).page(anyInt())
                .sortBy(DiscoverMovieSortBy.VOTE_AVERAGE_DESC)
        )).thenReturn(getMovieResultsPage());
        assertThat(tmDbService.getFilteredMovies(YEAR_2020, ONE)).isEqualTo(Set.of(ONE));
    }

    @SneakyThrows
    @Test
    @DisplayName("Verify getFilteredTvShows() method works")
    public void getFilteredTvShows_ValidResponse_ReturnSetIds() {
        when(discoverTvParamBuilder.firstAirDateYear(anyInt()))
                .thenReturn(discoverTvParamBuilder);
        when(discoverTvParamBuilder.withoutGenres(anyList()))
                .thenReturn(discoverTvParamBuilder);
        when(discoverTvParamBuilder.voteAverageGte(anyDouble()))
                .thenReturn(discoverTvParamBuilder);
        when(discoverTvParamBuilder.voteCountGte(anyDouble())).thenReturn(discoverTvParamBuilder);
        when(discoverTvParamBuilder.page(anyInt())).thenReturn(discoverTvParamBuilder);
        when(tmdbDiscover.getTv(discoverTvParamBuilder
                .firstAirDateYear(anyInt())
                .withoutGenres(anyList())
                .voteAverageGte(anyDouble())
                .voteCountGte(anyDouble()).page(anyInt())
                .sortBy(DiscoverTvSortBy.VOTE_AVERAGE_DESC)
        )).thenReturn(getTvSeriesResultsPage());
        assertThat(tmDbService.getFilteredTvShows(YEAR_2020, ONE)).isEqualTo(Set.of(TWO));
    }

    private ReleaseDateResults getReleaseDates() {
        final ReleaseDate releaseDate = new ReleaseDate();
        releaseDate.setCertification(AGE_RATING_G);
        final ReleaseInfo releaseInfo = new ReleaseInfo();
        releaseInfo.setReleaseDates(List.of(releaseDate));
        final ReleaseDateResults releaseDateResults = new ReleaseDateResults();
        releaseDateResults.setResults(List.of(releaseInfo));
        return releaseDateResults;
    }

    private TvSeriesResultsPage getTvSeriesResultsPage() {
        final TvSeries tvSeries = new TvSeries();
        tvSeries.setId(TWO);
        tvSeries.setOverview(OVERVIEW);
        tvSeries.setVoteAverage(VOTE_AVERAGE_8);
        tvSeries.setVoteCount(MIN_VOTE_COUNT);
        tvSeries.setPosterPath(POSTER_PATH);
        tvSeries.setFirstAirDate(RELEASE_DATE);
        tvSeries.setName(TITLE);
        tvSeries.setGenreIds(List.of(1));
        final TvSeriesResultsPage tvSeriesResultsPage = new TvSeriesResultsPage();
        tvSeriesResultsPage.setId(ONE);
        tvSeriesResultsPage.setPage(ONE);
        tvSeriesResultsPage.setResults(List.of(tvSeries));
        return tvSeriesResultsPage;
    }

    private MovieResultsPage getMovieResultsPage() {
        final MovieResultsPage movieResultsPage = new MovieResultsPage();
        final Movie movie = new Movie();
        movie.setId(ONE);
        movie.setOverview(OVERVIEW);
        movie.setVoteAverage(VOTE_AVERAGE_8);
        movie.setVoteCount(MIN_VOTE_COUNT);
        movie.setPosterPath(POSTER_PATH);
        movie.setReleaseDate(RELEASE_DATE);
        movie.setVideo(true);
        movie.setTitle(TITLE);
        movie.setGenreIds(List.of(1));
        final List<Movie> movieList = List.of(movie);
        movieResultsPage.setResults(movieList);
        movieResultsPage.setPage(ONE);
        return movieResultsPage;
    }

    private ContentRatingResults getRatingResults() {
        final ContentRating contentRating = new ContentRating();
        contentRating.setRating(AGE_RATING_G);
        final ContentRatingResults contentRatings = new ContentRatingResults();
        contentRatings.setResults(List.of(contentRating));
        return contentRatings;
    }

    private ReviewResultsPage getReviewResultsPage() {
        final ReviewResultsPage reviewResultsPage = new ReviewResultsPage();
        reviewResultsPage.setId(ONE);
        reviewResultsPage.setTotalPages(ONE);
        reviewResultsPage.setPage(ONE);
        reviewResultsPage.setResults(getListReviews());
        return reviewResultsPage;
    }

    private List<Review> getListReviews() {
        final Review review = new Review();
        final AuthorDetails authorDetails = new AuthorDetails();
        authorDetails.setRating("6");
        authorDetails.setAvatarPath(PATH);
        review.setAuthor(AUTHOR);
        review.setContent(CONTENT_STRING);
        review.setId(ID_STRING);
        review.setAuthorDetails(authorDetails);
        return List.of(review);
    }

    private MovieDb getMovieDb() {
        final MovieDb movieDb = new MovieDb();
        movieDb.setId(ONE);
        return movieDb;
    }

    private TvSeriesDb getTvSeriesDb() {
        final TvSeriesDb tvSeriesDb = new TvSeriesDb();
        tvSeriesDb.setId(TWO);
        return tvSeriesDb;
    }

    private VideoResults getVideoResults() {
        final Video video = new Video();
        video.setType(TRAILER);
        video.setKey(PATH);
        final VideoResults videoResults = new VideoResults();
        videoResults.setResults(List.of(video));
        return videoResults;
    }

    private List<Artwork> getListArtworks() {
        final Artwork artwork = new Artwork();
        artwork.setFilePath(PATH);
        return List.of(artwork);
    }
}
