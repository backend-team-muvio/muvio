package org.cyberrealm.tech.muvio.service.impl;

import static org.cyberrealm.tech.muvio.common.Constants.BACK_OFF;
import static org.cyberrealm.tech.muvio.common.Constants.IMAGE_PATH_W200;
import static org.cyberrealm.tech.muvio.common.Constants.MAX_ATTEMPTS;
import static org.cyberrealm.tech.muvio.common.Constants.MIN_VOTE_COUNT;
import static org.cyberrealm.tech.muvio.common.Constants.TEASER;
import static org.cyberrealm.tech.muvio.common.Constants.TRAILER;
import static org.cyberrealm.tech.muvio.common.Constants.YOUTUBE_PATH;

import dev.brachtendorf.jimagehash.hash.Hash;
import info.movito.themoviedbapi.TmdbDiscover;
import info.movito.themoviedbapi.TmdbMovieLists;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.TmdbSearch;
import info.movito.themoviedbapi.TmdbTvSeries;
import info.movito.themoviedbapi.TmdbTvSeriesLists;
import info.movito.themoviedbapi.model.core.IdElement;
import info.movito.themoviedbapi.model.core.Review;
import info.movito.themoviedbapi.model.core.ReviewResultsPage;
import info.movito.themoviedbapi.model.core.image.Artwork;
import info.movito.themoviedbapi.model.core.video.VideoResults;
import info.movito.themoviedbapi.model.movies.MovieDb;
import info.movito.themoviedbapi.model.movies.ReleaseDate;
import info.movito.themoviedbapi.model.reviews.AuthorDetails;
import info.movito.themoviedbapi.model.tv.series.ContentRating;
import info.movito.themoviedbapi.model.tv.series.TvSeriesDb;
import info.movito.themoviedbapi.tools.TmdbException;
import info.movito.themoviedbapi.tools.appendtoresponse.MovieAppendToResponse;
import info.movito.themoviedbapi.tools.appendtoresponse.TvSeriesAppendToResponse;
import info.movito.themoviedbapi.tools.builders.discover.DiscoverMovieParamBuilder;
import info.movito.themoviedbapi.tools.builders.discover.DiscoverTvParamBuilder;
import info.movito.themoviedbapi.tools.sortby.DiscoverMovieSortBy;
import info.movito.themoviedbapi.tools.sortby.DiscoverTvSortBy;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.cyberrealm.tech.muvio.exception.TmdbServiceException;
import org.cyberrealm.tech.muvio.service.ImageSimilarityService;
import org.cyberrealm.tech.muvio.service.TmDbService;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TmDbServiceImpl implements TmDbService {
    private static final double MIN_RATE = 5.0;
    private static final int FIRST_PAGE = 1;
    private static final int MAX_NUMBER_OF_PHOTOS = 6;
    private static final int MAX_NUMBER_OF_REVIEWS = 3;
    private static final Semaphore SEMAPHORE = new Semaphore(100, true);
    private static final List<Integer> FORBIDDEN_GENRE_IDS =
            List.of(16, 99, 36, 27, 10402, 10752, 10763, 10764, 10768, 10767);

    private final TmdbMovies tmdbMovies;
    private final TmdbTvSeries tmdbTvSeries;
    private final TmdbMovieLists tmdbMovieLists;
    private final TmdbTvSeriesLists tmdbTvSeriesLists;
    private final TmdbSearch tmdbSearch;
    private final TmdbDiscover tmdbDiscover;
    private final DiscoverMovieParamBuilder discoverMovieParamBuilder;
    private final DiscoverTvParamBuilder discoverTvParamBuilder;
    private final ImageSimilarityService imageSimilarityService;

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public Set<Integer> fetchPopularMovies(String language, int page, String region) {
        return executeTmDbCall(() -> tmdbMovieLists.getPopular(language, page, region)
                        .getResults().stream()
                        .filter(movie ->
                                !new HashSet<>(FORBIDDEN_GENRE_IDS)
                                        .containsAll(movie.getGenreIds())
                                        && movie.getVoteAverage() != null
                                        && movie.getVoteAverage() > MIN_RATE
                                        && movie.getVideo() != null
                                        && movie.getPosterPath() != null
                                        && movie.getOverview() != null
                                        && movie.getReleaseDate() != null
                                        && movie.getVoteCount() >= MIN_VOTE_COUNT)
                        .map(IdElement::getId).collect(Collectors.toSet()),
                "Failed to fetch popular movies from TmDb");
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public Set<Integer> fetchPopularTvSerials(String language, int page) {
        return executeTmDbCall(() -> tmdbTvSeriesLists.getPopular(language, page).getResults()
                        .stream().filter(tvSeries ->
                                !new HashSet<>(FORBIDDEN_GENRE_IDS)
                                        .containsAll(tvSeries.getGenreIds())
                                        && tvSeries.getVoteAverage() != null
                                        && tvSeries.getVoteAverage() > MIN_RATE
                                        && tvSeries.getPosterPath() != null
                                        && tvSeries.getOverview() != null
                                        && tvSeries.getFirstAirDate() != null
                                        && tvSeries.getVoteCount() >= MIN_VOTE_COUNT)
                        .map(IdElement::getId).collect(Collectors.toSet()),
                "Failed to fetch popular TVSerials from TmDb by page: " + page);
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public Optional<Integer> searchMovies(String query, String language, String region) {
        return executeTmDbCall(() -> tmdbSearch.searchMovie(query, false, language,
                                null, FIRST_PAGE, region, null)
                        .getResults().stream()
                        .filter(movie ->
                                !new HashSet<>(FORBIDDEN_GENRE_IDS)
                                        .containsAll(movie.getGenreIds())
                                        && movie.getTitle().equalsIgnoreCase(query))
                        .map(IdElement::getId).findFirst(),
                "Failed to find movie info from TmDb by query " + query);
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public Optional<Integer> searchTvSeries(String query, String language) {
        return executeTmDbCall(() -> tmdbSearch.searchTv(query, null, false, language,
                                FIRST_PAGE, null)
                        .getResults().stream()
                        .filter(series ->
                                !new HashSet<>(FORBIDDEN_GENRE_IDS)
                                        .containsAll(series.getGenreIds())
                                        && series.getName().equalsIgnoreCase(query))
                        .map(IdElement::getId).findFirst(),
                "Failed to find tv series info from TmDb by query " + query);
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public Set<Integer> getFilteredMovies(int year, int page) {
        return executeTmDbCall(() -> tmdbDiscover.getMovie(discoverMovieParamBuilder.year(year)
                                .withoutGenres(FORBIDDEN_GENRE_IDS)
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
                                .withoutGenres(FORBIDDEN_GENRE_IDS)
                                .firstAirDateYear(year).voteAverageGte(MIN_RATE)
                                .voteCountGte(MIN_VOTE_COUNT).page(page)
                                .sortBy(DiscoverTvSortBy.VOTE_AVERAGE_DESC))
                        .getResults().stream().map(IdElement::getId).collect(Collectors.toSet()),
                "Failed to filter tv shows from TmDb");
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public MovieDb fetchFullMovieDetails(int movieId, String language) {
        return executeTmDbCall(() -> tmdbMovies.getDetails(movieId, language,
                MovieAppendToResponse.CREDITS,
                MovieAppendToResponse.VIDEOS,
                MovieAppendToResponse.IMAGES,
                MovieAppendToResponse.KEYWORDS,
                MovieAppendToResponse.REVIEWS,
                MovieAppendToResponse.RELEASE_DATES
        ), "Can't load full movie details by movieId: " + movieId);
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public TvSeriesDb fetchFullTvSeriesDetails(int serialId, String language) {
        return executeTmDbCall(() -> tmdbTvSeries.getDetails(serialId, language,
                TvSeriesAppendToResponse.CREDITS,
                TvSeriesAppendToResponse.VIDEOS,
                TvSeriesAppendToResponse.IMAGES,
                TvSeriesAppendToResponse.KEYWORDS,
                TvSeriesAppendToResponse.REVIEWS,
                TvSeriesAppendToResponse.CONTENT_RATINGS
        ), "Can't load full tv series details by serialId: " + serialId);
    }

    @Override
    public String extractTrailer(VideoResults videos) {
        if (videos == null || videos.getResults() == null) {
            return null;
        }
        return Stream.of(TRAILER, TEASER)
                .map(type -> getTrailerLink(videos, type))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElse(null);
    }

    @Override
    public Set<String> extractPhotos(List<Artwork> backdrops, String posterPath) {
        if (backdrops == null || backdrops.isEmpty()) {
            return Collections.emptySet();
        }
        List<String> imagePaths = backdrops.stream()
                .filter(artwork -> artwork.getFilePath() != null)
                .sorted(Comparator.comparing(Artwork::getVoteAverage,
                        Comparator.nullsLast(Double::compareTo)).reversed())
                .map(artwork -> IMAGE_PATH_W200 + artwork.getFilePath())
                .toList();

        Set<Hash> seenHashes = new HashSet<>();
        Set<String> uniqueImagePaths = new LinkedHashSet<>();

        imageSimilarityService.addIfUniqueHash(posterPath, seenHashes, uniqueImagePaths);
        uniqueImagePaths.clear();

        for (String filePath : imagePaths) {
            imageSimilarityService.addIfUniqueHash(filePath, seenHashes, uniqueImagePaths);
            if (uniqueImagePaths.size() >= MAX_NUMBER_OF_PHOTOS) {
                break;
            }
        }
        return uniqueImagePaths;
    }

    @Override
    public List<Review> extractReviews(ReviewResultsPage reviewPage) {
        if (reviewPage == null || reviewPage.getResults() == null) {
            return Collections.emptyList();
        }

        return reviewPage.getResults().stream()
                .filter(review -> review.getAuthorDetails() != null
                        && review.getAuthorDetails().getRating() != null)
                .sorted(getReviewComparator())
                .limit(MAX_NUMBER_OF_REVIEWS)
                .map(this::updateReviewAvatar)
                .collect(Collectors.toList());
    }

    @Override
    public Set<String> extractMovieRatings(MovieDb movie) {
        if (movie.getReleaseDates() == null || movie.getReleaseDates().getResults() == null) {
            return Collections.emptySet();
        }
        return movie.getReleaseDates().getResults().stream()
                .flatMap(info -> info.getReleaseDates().stream()
                        .map(ReleaseDate::getCertification))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> extractTvRatings(TvSeriesDb tvSeries) {
        if (tvSeries.getContentRatings() == null
                || tvSeries.getContentRatings().getResults() == null) {
            return Collections.emptySet();
        }
        return tvSeries.getContentRatings().getResults().stream()
                .map(ContentRating::getRating)
                .collect(Collectors.toSet());
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
                review.getAuthorDetails().setAvatarPath(IMAGE_PATH_W200 + avatarPath);
            } else {
                review.getAuthorDetails().setAvatarPath(null);
            }
        }
        return review;
    }

    private Comparator<Review> getReviewComparator() {
        return Comparator.comparing(
                review -> Objects.requireNonNull(Optional.ofNullable(review.getAuthorDetails())
                        .map(AuthorDetails::getRating)
                        .map(Double::parseDouble)
                        .orElse(null)),
                Comparator.nullsLast(Comparator.reverseOrder())
        );
    }

    private <T> T executeTmDbCall(Callable<T> callable, String errorMessage) {
        try {
            SEMAPHORE.acquire();
            return callable.call();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TmdbServiceException("Thread was interrupted while waiting for semaphore", e);
        } catch (TmdbException e) {
            throw new TmdbServiceException(errorMessage, e);
        } catch (Exception e) {
            throw new TmdbServiceException("Unexpected error during TMDb API call", e);
        } finally {
            SEMAPHORE.release();
        }
    }
}
