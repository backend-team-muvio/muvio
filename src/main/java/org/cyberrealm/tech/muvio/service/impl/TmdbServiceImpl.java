package org.cyberrealm.tech.muvio.service.impl;

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.TmdbTvSeries;
import info.movito.themoviedbapi.model.core.Movie;
import info.movito.themoviedbapi.model.core.Review;
import info.movito.themoviedbapi.model.core.ReviewResultsPage;
import info.movito.themoviedbapi.model.core.TvKeywords;
import info.movito.themoviedbapi.model.core.TvSeries;
import info.movito.themoviedbapi.model.core.image.Artwork;
import info.movito.themoviedbapi.model.core.video.VideoResults;
import info.movito.themoviedbapi.model.movies.Credits;
import info.movito.themoviedbapi.model.movies.KeywordResults;
import info.movito.themoviedbapi.model.movies.MovieDb;
import info.movito.themoviedbapi.model.movies.ReleaseInfo;
import info.movito.themoviedbapi.model.tv.series.TvSeriesDb;
import info.movito.themoviedbapi.tools.TmdbException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.cyberrealm.tech.muvio.exception.TmdbServiceException;
import org.cyberrealm.tech.muvio.service.TmdbService;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TmdbServiceImpl implements TmdbService {
    public static final String IMAGE_PATH = "https://image.tmdb.org/t/p/w500";
    private static final int MAX_ATTEMPTS = 12;
    private static final int BACK_OFF = 10000;
    private static final String YOUTUBE_PATH = "https://www.youtube.com/watch?v=";
    private static final String TRAILER = "Trailer";
    private static final String TEASER = "Teaser";
    private static final int FIRST_PAGE = 1;
    private static final int MAX_NUMBER_OF_RECORDS = 6;
    private final TmdbApi tmdbApi;

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public TmdbMovies getTmdbMovies() {
        return tmdbApi.getMovies();
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public List<Movie> fetchPopularMovies(int fromPage, int toPage, String language,
                                          String location, ForkJoinPool pool) {
        return fetchPopularItems(
                fromPage,
                toPage,
                page -> {
                    try {
                        return tmdbApi.getMovieLists().getPopular(language, page, location)
                                .getResults().stream()
                                .filter(movie -> movie.getVoteAverage() > 5.0)
                                .filter(movie -> movie.getVideo() != null)
                                .filter(movie -> movie.getPosterPath() != null)
                                .filter(movie -> movie.getOverview() != null)
                                .toList();
                    } catch (TmdbException e) {
                        throw new TmdbServiceException("Failed to fetch popular movies from TMDB",
                                e);
                    }
                },
                pool
        );
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public MovieDb fetchMovieDetails(TmdbMovies tmdbMovies, int movieId, String language) {
        try {
            return tmdbMovies.getDetails(movieId, language);
        } catch (TmdbException e) {
            throw new TmdbServiceException("Can't load movie details by movieId: " + movieId
                    + e.getMessage());
        }
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public Credits fetchMovieCredits(TmdbMovies tmdbMovies, int movieId, String language) {
        try {
            return tmdbMovies.getCredits(movieId, language);
        } catch (TmdbException e) {
            throw new TmdbServiceException("Can't load credits by movieId: " + movieId
                    + e.getMessage());
        }
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public String fetchMovieTrailer(TmdbMovies tmdbMovies, int movieId, String language) {
        return fetchTrailer(() -> {
            try {
                return tmdbMovies.getVideos(movieId, language);
            } catch (TmdbException e) {
                throw new TmdbServiceException("Failed to fetch trailer from TMDB", e);
            }
        });
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public Set<String> fetchMoviePhotos(TmdbMovies tmdbMovies, String language, int movieId) {
        return fetchPhotos(() -> {
            try {
                return tmdbMovies.getImages(movieId, "null").getBackdrops();
            } catch (TmdbException e) {
                throw new TmdbServiceException("Failed to fetch photos from TMDB", e);
            }
        });
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public KeywordResults fetchMovieKeywords(TmdbMovies tmdbMovies, int movieId) {
        try {
            return tmdbMovies.getKeywords(movieId);
        } catch (TmdbException e) {
            throw new TmdbServiceException("Failed to fetch keywords from TMDB", e);
        }
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public List<ReleaseInfo> fetchReleaseInfo(TmdbMovies tmdbMovies, int movieId) {
        try {
            return tmdbMovies.getReleaseDates(movieId).getResults();
        } catch (TmdbException e) {
            throw new TmdbServiceException("Failed to fetch release info from TMDB", e);
        }
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public List<Review> fetchMovieReviews(TmdbMovies tmdbMovies, String language, int movieId) {
        return fetchAllReviews(page -> {
            try {
                return tmdbMovies.getReviews(movieId, language, page);
            } catch (TmdbException e) {
                throw new TmdbServiceException(
                        "Failed to fetch reviews from TMDB: " + e.getMessage(), e);
            }
        });
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public TmdbTvSeries getTmdbTvSerials() {
        return tmdbApi.getTvSeries();
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public List<TvSeries> fetchPopularTvSerials(int fromPage, int toPage, String language,
                                                String location, ForkJoinPool pool) {
        return fetchPopularItems(
                fromPage,
                toPage,
                page -> {
                    try {
                        return tmdbApi.getTvSeriesLists().getPopular(language, page).getResults()
                                .stream()
                                .filter(tvSeries -> tvSeries.getVoteAverage() > 5.0)
                                .filter(tvSeries -> tvSeries.getPosterPath() != null)
                                .filter(tvSeries -> tvSeries.getOverview() != null)
                                .filter(tvSeries -> tvSeries.getFirstAirDate() != null)
                                .toList();
                    } catch (TmdbException e) {
                        throw new TmdbServiceException(
                                "Failed to fetch popular TVSerials from TMDB", e);
                    }
                },
                pool
        );
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public TvSeriesDb fetchTvSerialsDetails(TmdbTvSeries tvSeries, int serialId, String language) {
        try {
            return tvSeries.getDetails(serialId, language);
        } catch (TmdbException e) {
            throw new TmdbServiceException("Can't load movie details by serialId: " + serialId
                    + e.getMessage());
        }
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public info.movito.themoviedbapi.model.tv.core.credits.Credits fetchTvSerialsCredits(
            TmdbTvSeries tvSeries, int serialId, String language) {
        try {
            return tvSeries.getCredits(serialId, language);
        } catch (TmdbException e) {
            throw new TmdbServiceException("Can't load credits by serialId: " + serialId
                    + e.getMessage());
        }
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public String fetchTvSerialsTrailer(TmdbTvSeries tvSeries, int serialId, String language) {
        return fetchTrailer(() -> {
            try {
                return tvSeries.getVideos(serialId, language);
            } catch (TmdbException e) {
                throw new TmdbServiceException("Failed to fetch trailer from TMDB", e);
            }
        });
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public Set<String> fetchTvSerialsPhotos(TmdbTvSeries tvSeries, String language, int serialId) {
        return fetchPhotos(() -> {
            try {
                return tvSeries.getImages(serialId, language).getBackdrops();
            } catch (TmdbException e) {
                throw new TmdbServiceException("Failed to fetch photos from TMDB", e);
            }
        });
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public TvKeywords fetchTvSerialsKeywords(TmdbTvSeries tvSeries, int serialId) {
        try {
            return tvSeries.getKeywords(serialId);
        } catch (TmdbException e) {
            throw new TmdbServiceException("Failed to fetch keywords from TMDB", e);
        }
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public List<Review> fetchTvSerialsReviews(TmdbTvSeries tvSeries, String language,
                                              int serialId) {
        return fetchAllReviews(page -> {
            try {
                return tvSeries.getReviews(serialId, language, page);
            } catch (TmdbException e) {
                throw new TmdbServiceException("Failed to fetch reviews from TMDB: "
                        + e.getMessage(), e);
            }
        });
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
                .map(this::updateReviewAvatar)
                .collect(Collectors.toList());
    }

    private String fetchTrailer(Supplier<VideoResults> videoSupplier) {
        VideoResults videos = videoSupplier.get();
        return getTrailerLink(videos, TRAILER)
                .orElse(getTrailerLink(videos, TEASER)
                        .orElse(null));
    }

    private <T> List<T> fetchPopularItems(int fromPage, int toPage, Function<Integer,
            List<T>> pageFetcher, ForkJoinPool pool) {
        try {
            return pool.submit(() -> IntStream.rangeClosed(fromPage, toPage).parallel()
                            .mapToObj(pageFetcher::apply)
                            .flatMap(Collection::stream)
                            .collect(Collectors.toList()))
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            throw new TmdbServiceException(
                    "Failed to process popular items with custom thread pool", e);
        }
    }

    private Set<String> fetchPhotos(Supplier<List<Artwork>> imagesSupplier) {
        List<Artwork> artworks = imagesSupplier.get();
        return artworks.stream()
                //.filter(artwork -> artwork.getAspectRatio() > 1.5)
                .limit(MAX_NUMBER_OF_RECORDS)
                .peek(artwork -> artwork.setFilePath(IMAGE_PATH + artwork.getFilePath()))
                .map(Artwork::getFilePath)
                .collect(Collectors.toSet());
    }

}
