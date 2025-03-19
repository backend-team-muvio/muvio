package org.cyberrealm.tech.muvio.service;

import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.TmdbTvSeries;
import info.movito.themoviedbapi.model.core.Movie;
import info.movito.themoviedbapi.model.core.Review;
import info.movito.themoviedbapi.model.core.TvKeywords;
import info.movito.themoviedbapi.model.core.TvSeries;
import info.movito.themoviedbapi.model.movies.Credits;
import info.movito.themoviedbapi.model.movies.KeywordResults;
import info.movito.themoviedbapi.model.movies.MovieDb;
import info.movito.themoviedbapi.model.movies.ReleaseInfo;
import info.movito.themoviedbapi.model.tv.series.TvSeriesDb;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

public interface TmdbService {

    TmdbMovies getTmdbMovies();

    List<Movie> fetchPopularMovies(int fromPage, int toPage, String language, String location,
                                   ForkJoinPool pool);

    MovieDb fetchMovieDetails(TmdbMovies tmdbMovies, int movieId, String language);

    Credits fetchMovieCredits(TmdbMovies tmdbMovies, int movieId, String language);

    String fetchMovieTrailer(TmdbMovies tmdbMovies, int movieId, String language);

    Set<String> fetchMoviePhotos(TmdbMovies tmdbMovies, String language, int movieId);

    KeywordResults fetchMovieKeywords(TmdbMovies tmdbMovies, int movieId);

    List<ReleaseInfo> fetchReleaseInfo(TmdbMovies tmdbMovies, int movieId);

    List<Review> fetchMovieReviews(TmdbMovies tmdbMovies, String language, int movieId);

    TmdbTvSeries getTmdbTvSerials();

    List<TvSeries> fetchPopularTvSerials(int fromPage, int toPage, String language, String location,
                                         ForkJoinPool pool);

    TvSeriesDb fetchTvSerialsDetails(TmdbTvSeries tvSeries, int serialId, String language);

    info.movito.themoviedbapi.model.tv.core.credits.Credits fetchTvSerialsCredits(
            TmdbTvSeries tvSeries, int serialId, String language);

    String fetchTvSerialsTrailer(TmdbTvSeries tvSeries, int serialId, String language);

    Set<String> fetchTvSerialsPhotos(TmdbTvSeries tvSeries, String language, int serialId);

    TvKeywords fetchTvSerialsKeywords(TmdbTvSeries tvSeries, int serialId);

    List<Review> fetchTvSerialsReviews(TmdbTvSeries tvSeries, String language, int serialId);
}
