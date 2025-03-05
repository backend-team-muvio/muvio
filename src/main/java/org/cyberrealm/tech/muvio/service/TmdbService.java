package org.cyberrealm.tech.muvio.service;

import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.model.core.MovieResultsPage;
import info.movito.themoviedbapi.model.movies.Credits;
import info.movito.themoviedbapi.model.movies.KeywordResults;
import info.movito.themoviedbapi.model.movies.MovieDb;
import info.movito.themoviedbapi.model.movies.ReleaseInfo;
import org.cyberrealm.tech.muvio.model.ReviewDb;

import java.util.List;
import java.util.Set;

public interface TmdbService {
    List<ReleaseInfo> getReleaseInfo(TmdbMovies tmdbMovies, int movieId);
    KeywordResults getKeywordResults(TmdbMovies tmdbMovies, int movieId);
    Credits getCredits(TmdbMovies tmdbMovies, int movieId, String language);
    MovieDb getMovieDb(int movieId, String language);
    MovieResultsPage getMovieResultsPage(String language, int page, String location);
    Set<String> getPhotos(TmdbMovies tmdbMovies, int movieId, String language);
    String getTrailer(TmdbMovies tmdbMovies, int movieId, String language);
    void deleteAll();

    List<ReviewDb> getReviews(TmdbMovies tmdbMovies, int movieId, String language);
}
