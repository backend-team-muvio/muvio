package org.cyberrealm.tech.muvio.service;

import info.movito.themoviedbapi.model.core.Review;
import info.movito.themoviedbapi.model.core.ReviewResultsPage;
import info.movito.themoviedbapi.model.core.image.Artwork;
import info.movito.themoviedbapi.model.core.video.VideoResults;
import info.movito.themoviedbapi.model.movies.MovieDb;
import info.movito.themoviedbapi.model.tv.series.TvSeriesDb;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TmDbService {
    Set<Integer> fetchPopularMovies(String language, int page, String region);

    Set<Integer> fetchPopularTvSerials(String language, int page);

    Optional<Integer> searchMovies(String query, String language, String region);

    Optional<Integer> searchTvSeries(String query, String language);

    Set<Integer> getFilteredMovies(int year, int page);

    Set<Integer> getFilteredTvShows(int year, int page);

    MovieDb fetchFullMovieDetails(int movieId, String language);

    TvSeriesDb fetchFullTvSeriesDetails(int serialId, String language);

    String extractTrailer(VideoResults videos);

    Set<String> extractPhotos(List<Artwork> backdrops, String posterPath);

    List<Review> extractReviews(ReviewResultsPage reviewPage);

    Set<String> extractMovieRatings(MovieDb movie);

    Set<String> extractTvRatings(TvSeriesDb tvSeries);
}
