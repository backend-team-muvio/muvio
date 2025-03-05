package org.cyberrealm.tech.muvio.service.impl;

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.model.core.MovieResultsPage;
import info.movito.themoviedbapi.model.core.Review;
import info.movito.themoviedbapi.model.core.ReviewResultsPage;
import info.movito.themoviedbapi.model.core.image.Artwork;
import info.movito.themoviedbapi.model.core.video.VideoResults;
import info.movito.themoviedbapi.model.movies.Credits;
import info.movito.themoviedbapi.model.movies.Images;
import info.movito.themoviedbapi.model.movies.KeywordResults;
import info.movito.themoviedbapi.model.movies.MovieDb;
import info.movito.themoviedbapi.model.movies.ReleaseInfo;
import info.movito.themoviedbapi.tools.TmdbException;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import lombok.RequiredArgsConstructor;
import org.cyberrealm.tech.muvio.exception.TmdbServiceException;
import org.cyberrealm.tech.muvio.model.ReviewDb;
import org.cyberrealm.tech.muvio.repository.actors.ActorRepository;
import org.cyberrealm.tech.muvio.repository.movies.MovieRepository;
import org.cyberrealm.tech.muvio.repository.reviews.ReviewRepository;
import org.cyberrealm.tech.muvio.service.TmdbService;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Retryable(retryFor = TmdbServiceException.class,
        maxAttempts = 3,
        backoff = @Backoff(delay = 20000))
public class TmdbApiServiceImpl implements TmdbService {
    private static final String YOUTUBE_PATH = "https://www.youtube.com/watch?v=";
    private static final String IMAGE_PATH = "https://image.tmdb.org/t/p/w500";
    private static final String TRAILER = "Trailer";
    private static final String TEASER = "Teaser";
    private static final int MAX_ATTEMPTS = 3;
    private static final int BACK_OFF = 20000;
    private final TmdbApi tmdbApi;
    private final MovieRepository movieRepository;
    private final ActorRepository actorRepository;
    private final ReviewRepository reviewRepository;

    @Retryable(retryFor = TmdbServiceException.class,
            maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public List<ReleaseInfo> getReleaseInfo(TmdbMovies tmdbMovies, int movieId) {
        try {
            return tmdbMovies.getReleaseDates(movieId).getResults();
        } catch (TmdbException e) {
            throw new TmdbServiceException("Can't find releaseInfo by movieId " + movieId, e);
        }
    }

    @Retryable(retryFor = TmdbServiceException.class,
            maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public KeywordResults getKeywordResults(TmdbMovies tmdbMovies, int movieId) {
        try {
            return tmdbMovies.getKeywords(movieId);
        } catch (TmdbException e) {
            throw new TmdbServiceException("Can't find  keywords by movieId " + movieId, e);
        }
    }

    @Retryable(retryFor = TmdbServiceException.class,
            maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public Credits getCredits(TmdbMovies tmdbMovies, int movieId, String language) {
        try {
            return tmdbMovies.getCredits(movieId, language);
        } catch (TmdbException e) {
            throw new TmdbServiceException("Can't find credits by movieId " + movieId, e);
        }
    }

    @Retryable(retryFor = TmdbServiceException.class,
            maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public MovieDb getMovieDb(int movieId, String language) {
        try {
            return tmdbApi.getMovies().getDetails(movieId, language);
        } catch (TmdbException e) {
            throw new TmdbServiceException("Can't find movieDb  by movieId " + movieId, e);
        }
    }

    @Retryable(retryFor = TmdbServiceException.class,
            maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public MovieResultsPage getMovieResultsPage(String language, int page, String location) {
        try {
            return tmdbApi.getMovieLists().getPopular(language, page, location);
        } catch (TmdbException e) {
            throw new TmdbServiceException("Failed to fetch popular movies", e);
        }
    }

    @Retryable(retryFor = TmdbServiceException.class,
            maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public Set<String> getPhotos(TmdbMovies tmdbMovies, int movieId, String language) {
        Images images;
        try {
            images = tmdbMovies.getImages(movieId, language);
        } catch (TmdbException e) {
            throw new TmdbServiceException("Can't find images by movieId " + movieId, e);
        }
        final Set<String> photos = new HashSet<>();
        addPhoto(Objects.requireNonNull(images).getPosters(), photos);
        addPhoto(images.getBackdrops(), photos);
        return photos;
    }

    private void addPhoto(List<Artwork> artworks, Set<String> photos) {
        artworks.forEach(poster -> {
            if (photos.size() < 9) {
                photos.add(IMAGE_PATH + poster.getFilePath());
            }
        });
    }

    @Retryable(retryFor = TmdbServiceException.class,
            maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public String getTrailer(TmdbMovies tmdbMovies, int movieId, String language) {
        final VideoResults videos;
        try {
            videos = tmdbMovies.getVideos(movieId, language);
        } catch (TmdbException e) {
            throw new TmdbServiceException("Can't find video by movieId " + movieId, e);
        }
        return getTrailerLink(videos, TRAILER)
                .orElse(getTrailerLink(videos, TEASER).orElse("no trailer"));
    }

    private Optional<String> getTrailerLink(VideoResults videos, String type) {
        return videos.getResults().stream()
                .filter(video -> video.getType().equals(type))
                .map(trailer -> YOUTUBE_PATH + trailer.getKey())
                .findFirst();
    }

    @Override
    public void deleteAll() {
        if (movieRepository != null) {
            movieRepository.deleteAll();
        }
        if (actorRepository != null) {
            actorRepository.deleteAll();
        }
        if (reviewRepository != null) {
            reviewRepository.deleteAll();
        }
    }

    @Retryable(retryFor = TmdbServiceException.class,
            maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public List<ReviewDb> getReviews(TmdbMovies tmdbMovies, int movieId, String language) {
        List<ReviewDb> reviewDbList = new ArrayList<>();
        int page = 1;
        ReviewResultsPage reviews = fetchReviews(tmdbMovies, movieId, language, page);
        do {
            List<ReviewDb> reviewsToSave = reviews.getResults().stream()
                    .map(this::mapToReview)
                    .toList();
            reviewDbList.addAll(reviewRepository.saveAll(reviewsToSave));
            page++;
            if (page <= reviews.getTotalPages()) {
                reviews = fetchReviews(tmdbMovies, movieId, language, page);
            }
        } while (page <= reviews.getTotalPages());
        return reviewDbList;
    }

    private ReviewResultsPage fetchReviews(TmdbMovies tmdbMovies, int movieId, String language, int page) {
        try {
            return tmdbMovies.getReviews(movieId, language, page);
        } catch (TmdbException e) {
            throw new TmdbServiceException("Can't find reviews by movieId " + movieId, e);
        }
    }

    private ReviewDb mapToReview(Review review) {
        ReviewDb reviewDb = new ReviewDb();
        reviewDb.setNickname(review.getAuthor());
        reviewDb.setTime(ZonedDateTime.parse(review.getCreatedAt(), DateTimeFormatter.ISO_DATE_TIME)
                .withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime());
        Optional.ofNullable(review.getAuthorDetails().getRating())
                .map(Double::valueOf)
                .ifPresent(reviewDb::setRating);
        reviewDb.setText(review.getContent());
        return reviewDb;
    }
}
