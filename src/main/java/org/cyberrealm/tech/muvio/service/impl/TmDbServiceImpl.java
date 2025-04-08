package org.cyberrealm.tech.muvio.service.impl;

import info.movito.themoviedbapi.TmdbDiscover;
import info.movito.themoviedbapi.TmdbMovieLists;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.TmdbSearch;
import info.movito.themoviedbapi.TmdbTvSeries;
import info.movito.themoviedbapi.TmdbTvSeriesLists;
import info.movito.themoviedbapi.model.core.IdElement;
import info.movito.themoviedbapi.model.core.Review;
import info.movito.themoviedbapi.model.core.ReviewResultsPage;
import info.movito.themoviedbapi.model.core.TvKeywords;
import info.movito.themoviedbapi.model.core.image.Artwork;
import info.movito.themoviedbapi.model.core.video.VideoResults;
import info.movito.themoviedbapi.model.movies.Credits;
import info.movito.themoviedbapi.model.movies.KeywordResults;
import info.movito.themoviedbapi.model.movies.MovieDb;
import info.movito.themoviedbapi.model.movies.ReleaseDate;
import info.movito.themoviedbapi.model.reviews.AuthorDetails;
import info.movito.themoviedbapi.model.tv.series.ContentRating;
import info.movito.themoviedbapi.model.tv.series.TvSeriesDb;
import info.movito.themoviedbapi.tools.TmdbException;
import info.movito.themoviedbapi.tools.builders.discover.DiscoverMovieParamBuilder;
import info.movito.themoviedbapi.tools.builders.discover.DiscoverTvParamBuilder;
import info.movito.themoviedbapi.tools.sortby.DiscoverMovieSortBy;
import info.movito.themoviedbapi.tools.sortby.DiscoverTvSortBy;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.cyberrealm.tech.muvio.exception.TmdbServiceException;
import org.cyberrealm.tech.muvio.service.TmDbService;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TmDbServiceImpl implements TmDbService {
    public static final String IMAGE_PATH = "https://image.tmdb.org/t/p/w500";
    public static final double MIN_RATE = 5.0;
    private static final double MIN_VOTE_COUNT = 100;
    private static final int MAX_ATTEMPTS = 12;
    private static final int BACK_OFF = 10000;
    private static final String YOUTUBE_PATH = "https://www.youtube.com/watch?v=";
    private static final String TRAILER = "Trailer";
    private static final String TEASER = "Teaser";
    private static final int FIRST_PAGE = 1;
    private static final int MAX_NUMBER_OF_PHOTOS = 6;
    private static final int MAX_NUMBER_OF_REVIEWS = 3;
    private static final Semaphore SEMAPHORE = new Semaphore(100, true);
    private final TmdbMovies tmdbMovies;
    private final TmdbTvSeries tmdbTvSeries;
    private final TmdbMovieLists tmdbMovieLists;
    private final TmdbTvSeriesLists tmdbTvSeriesLists;
    private final TmdbSearch tmdbSearch;
    private final TmdbDiscover tmdbDiscover;
    private final DiscoverMovieParamBuilder discoverMovieParamBuilder;
    private final DiscoverTvParamBuilder discoverTvParamBuilder;

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public Set<Integer> fetchPopularMovies(String language, int page, String location) {
        return executeTmDbCall(() -> tmdbMovieLists.getPopular(language, page, location)
                                .getResults().stream().filter(
                                        movie -> movie.getVoteAverage() > MIN_RATE
                                                && movie.getVideo() != null
                                                && movie.getPosterPath() != null
                                                && movie.getOverview() != null
                                                && movie.getReleaseDate() != null)
                        .map(IdElement::getId).collect(Collectors.toSet()),
                "Failed to fetch popular movies from TmDb");
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public MovieDb fetchMovieDetails(int movieId, String language) {
        return executeTmDbCall(() -> tmdbMovies.getDetails(movieId, language),
                "Can't load movie details by movieId: " + movieId);
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public Credits fetchMovieCredits(int movieId, String language) {
        return executeTmDbCall(() -> tmdbMovies.getCredits(movieId, language),
                "Can't load credits by movieId: " + movieId);
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public String fetchMovieTrailer(int movieId, String language) {
        return fetchTrailer(() -> executeTmDbCall(() -> tmdbMovies.getVideos(movieId, language),
                "Failed to fetch trailer from TmDb by movieId: " + movieId));
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public Set<String> fetchMoviePhotos(String language, int movieId) {
        return fetchPhotos(() -> executeTmDbCall(() -> tmdbMovies.getImages(movieId, language)
                        .getBackdrops(),
                "Failed to fetch photos from TmDb by movieId: " + movieId));
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public KeywordResults fetchMovieKeywords(int movieId) {
        return executeTmDbCall(() -> tmdbMovies.getKeywords(movieId),
                "Failed to fetch keywords from TmDb by movieId: " + movieId);
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public List<Review> fetchMovieReviews(String language, int movieId) {
        return fetchAllReviews(page -> executeTmDbCall(() -> tmdbMovies.getReviews(
                movieId, language, page),
                "Failed to fetch reviews from TmDb by movieId: " + movieId));
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public Set<Integer> fetchPopularTvSerials(String language, int page) {
        return executeTmDbCall(() -> tmdbTvSeriesLists.getPopular(language, page).getResults()
                        .stream().filter(tvSeries -> tvSeries.getVoteAverage() > MIN_RATE
                        && tvSeries.getPosterPath() != null
                        && tvSeries.getOverview() != null
                        && tvSeries.getFirstAirDate() != null)
                .map(IdElement::getId).collect(Collectors.toSet()),
                "Failed to fetch popular TVSerials from TmDb by page: " + page);
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public TvSeriesDb fetchTvSerialsDetails(int serialId, String language) {
        return executeTmDbCall(() -> tmdbTvSeries.getDetails(serialId, language),
                "Can't load movie details by serialId: " + serialId);
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public info.movito.themoviedbapi.model.tv.core.credits.Credits fetchTvSerialsCredits(
            int serialId, String language) {
        return executeTmDbCall(() -> tmdbTvSeries.getCredits(serialId, language),
                "Can't load credits by serialId: " + serialId);
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public String fetchTvSerialsTrailer(int serialId, String language) {
        return fetchTrailer(() -> executeTmDbCall(() -> tmdbTvSeries.getVideos(serialId, language),
                        "Failed to fetch trailer from TmDb by serialId " + serialId));
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public Set<String> fetchTvSerialsPhotos(String language, int serialId) {
        return fetchPhotos(() -> executeTmDbCall(() -> tmdbTvSeries.getImages(serialId, language)
                        .getBackdrops(),
                        "Failed to fetch photos from TmDb by serialId " + serialId));
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public TvKeywords fetchTvSerialsKeywords(int serialId) {
        return executeTmDbCall(() -> tmdbTvSeries.getKeywords(serialId),
                "Failed to fetch keywords from TmDb by serialId " + serialId);
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public List<Review> fetchTvSerialsReviews(String language, int serialId) {
        return fetchAllReviews(page -> executeTmDbCall(() -> tmdbTvSeries
                        .getReviews(serialId, language, page),
                        "Failed to fetch reviews from TmDb by serialId "
                                + serialId + " and page " + page));
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public Set<String> fetchTmDbTvRatings(int seriesId) {
        return executeTmDbCall(() -> tmdbTvSeries.getContentRatings(seriesId).getResults().stream()
                .map(ContentRating::getRating).collect(Collectors.toSet()),
                "Failed to fetch ratings from TmDb by seriesId " + seriesId);
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public Set<String> fetchTmDbMovieRatings(int movieId) {
        return executeTmDbCall(() -> tmdbMovies.getReleaseDates(movieId).getResults().stream()
                .flatMap(info -> info.getReleaseDates().stream()
                        .map(ReleaseDate::getCertification)).collect(Collectors.toSet()),
                "Failed to fetch release info from TmDb by movieId " + movieId);
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public Optional<Integer> searchMovies(String query, String language, String region) {
        return executeTmDbCall(() -> tmdbSearch.searchMovie(query, false, language,
                                null, FIRST_PAGE, region, null)
                .getResults().stream().filter(movie -> movie.getTitle().equalsIgnoreCase(query))
                .map(IdElement::getId).findFirst(),
                "Failed to find movie info from TmDb by query " + query);
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public Optional<Integer> searchTvSeries(String query, String language) {
        return executeTmDbCall(() -> tmdbSearch.searchTv(query, null, false,
                                language, FIRST_PAGE, null)
                .getResults().stream().filter(series -> series.getName().equalsIgnoreCase(query))
                .map(IdElement::getId).findFirst(),
                "Failed to find tv series info from TmDb by query " + query);
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public Set<Integer> getFilteredMovies(int year, int page) {
        return executeTmDbCall(() -> tmdbDiscover.getMovie(discoverMovieParamBuilder.year(year)
                                .voteAverageGte(MIN_RATE).voteCountGte(MIN_VOTE_COUNT).page(page)
                                .sortBy(DiscoverMovieSortBy.VOTE_AVERAGE_DESC))
                        .getResults().stream().map(IdElement::getId).collect(Collectors.toSet()),
                "Failed to filter movies from TmDb by year " + year + " and page " + page);
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public Set<Integer> getFilteredTvShows(int year, int page) {
        return executeTmDbCall(() -> tmdbDiscover.getTv(discoverTvParamBuilder
                                .firstAirDateYear(year).voteAverageGte(MIN_RATE)
                                .voteCountGte(MIN_VOTE_COUNT).page(page)
                                .sortBy(DiscoverTvSortBy.VOTE_AVERAGE_DESC))
                        .getResults().stream().map(IdElement::getId).collect(Collectors.toSet()),
                "Failed to filter tv shows from TmDb");
    }

    private Optional<String> getTrailerLink(VideoResults videos, String type) {
        return videos.getResults().stream()
                .filter(video -> video.getType().equals(type))
                .map(trailer -> YOUTUBE_PATH + trailer.getKey())
                .findFirst();
    }

    private Review updateReviewAvatar(Review review) {
        if (review.getAuthorDetails() != null) {
            String avatarPath = review.getAuthorDetails().getAvatarPath();
            if (avatarPath != null) {
                review.getAuthorDetails().setAvatarPath(IMAGE_PATH + avatarPath);
            } else {
                review.getAuthorDetails().setAvatarPath(null);
            }
        }
        return review;
    }

    private List<Review> fetchAllReviews(Function<Integer, ReviewResultsPage> reviewFetcher) {
        ReviewResultsPage firstPage = reviewFetcher.apply(FIRST_PAGE);
        final List<Review> allReviews = new ArrayList<>();
        int totalPages = firstPage.getTotalPages();
        for (int page = FIRST_PAGE; page <= totalPages; page++) {
            ReviewResultsPage reviewPage = reviewFetcher.apply(page);
            if (reviewPage != null && reviewPage.getResults() != null) {
                allReviews.addAll(reviewPage.getResults());
            }
        }
        return allReviews.stream()
                .sorted(getReviewComparator())
                .limit(MAX_NUMBER_OF_REVIEWS)
                .map(this::updateReviewAvatar)
                .collect(Collectors.toList());
    }

    private Comparator<Review> getReviewComparator() {
        return Comparator.comparing(
                review -> Optional.ofNullable(review.getAuthorDetails())
                        .map(AuthorDetails::getRating)
                        .map(Double::parseDouble)
                        .orElse(null),
                Comparator.nullsLast(Comparator.reverseOrder())
        );
    }

    private String fetchTrailer(Supplier<VideoResults> videoSupplier) {
        VideoResults videos = videoSupplier.get();
        return Stream.of(TRAILER, TEASER)
                .map(type -> getTrailerLink(videos, type))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElse(null);
    }

    private Set<String> fetchPhotos(Supplier<List<Artwork>> imagesSupplier) {
        List<Artwork> artworks = imagesSupplier.get();
        return artworks.stream()
                .limit(MAX_NUMBER_OF_PHOTOS)
                .peek(artwork -> artwork.setFilePath(IMAGE_PATH + artwork.getFilePath()))
                .map(Artwork::getFilePath)
                .collect(Collectors.toSet());
    }

    private <T> T executeTmDbCall(Callable<T> callable, String errorMessage) {
        try {
            SEMAPHORE.acquire();
            return callable.call();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TmdbServiceException("Thread was interrupted while waiting for semaphore",
                    e);
        } catch (TmdbException e) {
            throw new TmdbServiceException(errorMessage, e);
        } catch (Exception e) {
            throw new TmdbServiceException("Unexpected error during TMDb API call", e);
        } finally {
            SEMAPHORE.release();
        }
    }
}
