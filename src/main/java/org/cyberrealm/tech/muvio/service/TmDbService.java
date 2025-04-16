package org.cyberrealm.tech.muvio.service;

import info.movito.themoviedbapi.model.core.Review;
import info.movito.themoviedbapi.model.core.TvKeywords;
import info.movito.themoviedbapi.model.movies.Credits;
import info.movito.themoviedbapi.model.movies.KeywordResults;
import info.movito.themoviedbapi.model.movies.MovieDb;
import info.movito.themoviedbapi.model.tv.series.TvSeriesDb;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TmDbService {

    Set<Integer> fetchPopularMovies(String language, int page, String region);

    MovieDb fetchMovieDetails(int movieId, String language);

    Credits fetchMovieCredits(int movieId, String language);

    String fetchMovieTrailer(int movieId, String language);

    Set<String> fetchMoviePhotos(String language, int movieId);

    KeywordResults fetchMovieKeywords(int movieId);

    List<Review> fetchMovieReviews(String language, int movieId);

    Set<Integer> fetchPopularTvSerials(String language, int page);

    TvSeriesDb fetchTvSerialsDetails(int serialId, String language);

    info.movito.themoviedbapi.model.tv.core.credits.Credits fetchTvSerialsCredits(
            int serialId, String language);

    String fetchTvSerialsTrailer(int serialId, String language);

    Set<String> fetchTvSerialsPhotos(String language, int serialId);

    TvKeywords fetchTvSerialsKeywords(int serialId);

    List<Review> fetchTvSerialsReviews(String language, int serialId);

    Set<String> fetchTmDbTvRatings(int seriesId);

    Set<String> fetchTmDbMovieRatings(int movieId);

    Optional<Integer> searchMovies(String query, String language, String region);

    Optional<Integer> searchTvSeries(String query, String language);

    Set<Integer> getFilteredMovies(int year, int page);

    Set<Integer> getFilteredTvShows(int year, int page);
}
