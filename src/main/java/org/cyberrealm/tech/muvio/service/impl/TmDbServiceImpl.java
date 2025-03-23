package org.cyberrealm.tech.muvio.service.impl;

import info.movito.themoviedbapi.TmdbMovieLists;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.TmdbTvSeries;
import info.movito.themoviedbapi.TmdbTvSeriesLists;
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
import info.movito.themoviedbapi.model.movies.ReleaseDate;
import info.movito.themoviedbapi.model.tv.series.ContentRating;
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
    private static final int MAX_ATTEMPTS = 12;
    private static final int BACK_OFF = 10000;
    private static final String YOUTUBE_PATH = "https://www.youtube.com/watch?v=";
    private static final String TRAILER = "Trailer";
    private static final String TEASER = "Teaser";
    private static final int FIRST_PAGE = 1;
    private static final int MAX_NUMBER_OF_RECORDS = 6;
    private final TmdbMovies tmdbMovies;
    private final TmdbTvSeries tmdbTvSeries;
    private final TmdbMovieLists tmdbMovieLists;
    private final TmdbTvSeriesLists tmdbTvSeriesLists;

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
                        return tmdbMovieLists.getPopular(language, page, location)
                                .getResults().stream()
                                .filter(movie -> movie.getVoteAverage() > MIN_RATE
                                        && movie.getVideo() != null
                                        && movie.getPosterPath() != null
                                        && movie.getOverview() != null
                                        && movie.getReleaseDate() != null)
                                .toList();
                    } catch (TmdbException e) {
                        throw new TmdbServiceException("Failed to fetch popular movies from TmDb",
                                e);
                    }
                },
                pool
        );
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public MovieDb fetchMovieDetails(int movieId, String language) {
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
    public Credits fetchMovieCredits(int movieId, String language) {
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
    public String fetchMovieTrailer(int movieId, String language) {
        return fetchTrailer(() -> {
            try {
                return tmdbMovies.getVideos(movieId, language);
            } catch (TmdbException e) {
                throw new TmdbServiceException("Failed to fetch trailer from TmDb", e);
            }
        });
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public Set<String> fetchMoviePhotos(String language, int movieId) {
        return fetchPhotos(() -> {
            try {
                return tmdbMovies.getImages(movieId, language).getBackdrops();
            } catch (TmdbException e) {
                throw new TmdbServiceException("Failed to fetch photos from TmDb", e);
            }
        });
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public KeywordResults fetchMovieKeywords(int movieId) {
        try {
            return tmdbMovies.getKeywords(movieId);
        } catch (TmdbException e) {
            throw new TmdbServiceException("Failed to fetch keywords from TmDb", e);
        }
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public List<Review> fetchMovieReviews(String language, int movieId) {
        return fetchAllReviews(page -> {
            try {
                return tmdbMovies.getReviews(movieId, language, page);
            } catch (TmdbException e) {
                throw new TmdbServiceException(
                        "Failed to fetch reviews from TmDb: " + e.getMessage(), e);
            }
        });
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
                        return tmdbTvSeriesLists.getPopular(language, page).getResults()
                                .stream()
                                .filter(tvSeries -> tvSeries.getVoteAverage() > MIN_RATE
                                        && tvSeries.getPosterPath() != null
                                        && tvSeries.getOverview() != null
                                        && tvSeries.getFirstAirDate() != null)
                                .toList();
                    } catch (TmdbException e) {
                        throw new TmdbServiceException(
                                "Failed to fetch popular TVSerials from TmDb", e);
                    }
                },
                pool
        );
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public TvSeriesDb fetchTvSerialsDetails(int serialId, String language) {
        try {
            return tmdbTvSeries.getDetails(serialId, language);
        } catch (TmdbException e) {
            throw new TmdbServiceException("Can't load movie details by serialId: " + serialId
                    + e.getMessage());
        }
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public info.movito.themoviedbapi.model.tv.core.credits.Credits fetchTvSerialsCredits(
            int serialId, String language) {
        try {
            return tmdbTvSeries.getCredits(serialId, language);
        } catch (TmdbException e) {
            throw new TmdbServiceException("Can't load credits by serialId: " + serialId
                    + e.getMessage());
        }
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public String fetchTvSerialsTrailer(int serialId, String language) {
        return fetchTrailer(() -> {
            try {
                return tmdbTvSeries.getVideos(serialId, language);
            } catch (TmdbException e) {
                throw new TmdbServiceException("Failed to fetch trailer from TmDb", e);
            }
        });
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public Set<String> fetchTvSerialsPhotos(String language, int serialId) {
        return fetchPhotos(() -> {
            try {
                return tmdbTvSeries.getImages(serialId, language).getBackdrops();
            } catch (TmdbException e) {
                throw new TmdbServiceException("Failed to fetch photos from TmDb", e);
            }
        });
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public TvKeywords fetchTvSerialsKeywords(int serialId) {
        try {
            return tmdbTvSeries.getKeywords(serialId);
        } catch (TmdbException e) {
            throw new TmdbServiceException("Failed to fetch keywords from TmDb", e);
        }
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public List<Review> fetchTvSerialsReviews(String language,
                                              int serialId) {
        return fetchAllReviews(page -> {
            try {
                return tmdbTvSeries.getReviews(serialId, language, page);
            } catch (TmdbException e) {
                throw new TmdbServiceException("Failed to fetch reviews from TmDb: "
                        + e.getMessage(), e);
            }
        });
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public Set<String> fetchTmDbTvRatings(int seriesId) {
        try {
            return tmdbTvSeries.getContentRatings(seriesId).getResults().stream()
                    .map(ContentRating::getRating).collect(Collectors.toSet());
        } catch (TmdbException e) {
            throw new TmdbServiceException("Failed to fetch ratings from TmDb: "
                    + e.getMessage(), e);
        }
    }

    @Retryable(retryFor = TmdbServiceException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public Set<String> fetchTmDbMovieRatings(int movieId) {
        try {
            return tmdbMovies.getReleaseDates(movieId).getResults().stream()
                    .flatMap(info -> info.getReleaseDates().stream()
                    .map(ReleaseDate::getCertification)).collect(Collectors.toSet());
        } catch (TmdbException e) {
            throw new TmdbServiceException("Failed to fetch release info from TmDb", e);
        }
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
        return Stream.of(TRAILER, TEASER)
                .map(type -> getTrailerLink(videos, type))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElse(null);
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
                .limit(MAX_NUMBER_OF_RECORDS)
                .peek(artwork -> artwork.setFilePath(IMAGE_PATH + artwork.getFilePath()))
                .map(Artwork::getFilePath)
                .collect(Collectors.toSet());
    }
}
