package org.cyberrealm.tech.muvio.service;

import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.model.core.Movie;
import info.movito.themoviedbapi.model.core.Review;
import info.movito.themoviedbapi.model.movies.Credits;
import info.movito.themoviedbapi.model.movies.KeywordResults;
import info.movito.themoviedbapi.model.movies.MovieDb;
import info.movito.themoviedbapi.model.movies.ReleaseInfo;
import java.util.List;
import java.util.Set;

public interface TmdbService {

    TmdbMovies getTmdbMovies();

    List<Movie> fetchPopularMovies(int fromPage, int toPage, String language, String location);

    MovieDb fetchMovieDetails(TmdbMovies tmdbMovies, int movieId, String language);

    Credits fetchMovieCredits(TmdbMovies tmdbMovies, int movieId, String language);

    String fetchTrailer(TmdbMovies tmdbMovies, int movieId, String language);

    Set<String> fetchPhotos(TmdbMovies tmdbMovies, String language, int movieId);

    KeywordResults fetchKeywords(TmdbMovies tmdbMovies, int movieId);

    List<ReleaseInfo> fetchReleaseInfo(TmdbMovies tmdbMovies, int movieId);

    List<Review> fetchMovieReviews(TmdbMovies tmdbMovies, String language, int movieId);
}
