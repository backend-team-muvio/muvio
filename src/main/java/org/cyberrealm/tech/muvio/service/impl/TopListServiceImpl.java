package org.cyberrealm.tech.muvio.service.impl;

import info.movito.themoviedbapi.model.keywords.Keyword;
import java.time.Year;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.cyberrealm.tech.muvio.model.TopLists;
import org.cyberrealm.tech.muvio.service.TopListService;
import org.springframework.stereotype.Service;

@Service
public class TopListServiceImpl implements TopListService {
    private static final String SUPERHERO = "superhero";
    private static final int START_YEAR = 2001;
    private static final int POPULARITY_LIMIT = 4;
    private static final int VOTE_COUNT_LIMIT = 1000;
    private static final int RATING_LIMIT = 7;
    private static final int LIMIT_REVENUE = 50_000_000;
    private static final int TWO = 2;
    private static final double IMDB_TOP_RATING_LIMIT = 8.0;
    private static final int TEN = 10;
    private static final int ONE = 1;

    @Override
    public Set<TopLists> putTopLists(List<Keyword> keywords, Double voteAverage,
                                     Integer voteCount, Double popularity, Integer releaseYear,
                                     Set<String> oscarWinningMedia, String title, Integer budget,
                                     Long revenue) {
        Set<TopLists> topLists = new HashSet<>();
        putSuperheroMovies(topLists, keywords, voteAverage);
        putIconicMovies(topLists, voteAverage, voteCount, popularity, releaseYear);
        putOscarWinningMedia(topLists, title, oscarWinningMedia);
        putBlockbustersDecade(topLists, releaseYear, voteAverage, voteCount, popularity,
                budget, revenue);
        putTopRatedImdbMovies(topLists, releaseYear, voteAverage, voteCount);
        if (topLists.isEmpty()) {
            return null;
        }
        return topLists;
    }

    @Override
    public Set<TopLists> putTopListsForTvShow(List<Keyword> keywords, Double voteAverage,
                                              Integer voteCount, Double popularity,
                                              Integer releaseYear, Set<String> emmyWinningSerials,
                                              String title) {
        Set<TopLists> topLists = new HashSet<>();

        putSuperheroMovies(topLists, keywords, voteAverage);
        putIconicMovies(topLists, voteAverage, voteCount, popularity, releaseYear);
        putBlockbustersDecadeForTvShows(topLists, releaseYear, voteAverage, voteCount, popularity);
        putTopRatedImdbMovies(topLists, releaseYear, voteAverage, voteCount);
        putEmmyWinningTvSerials(topLists, title, emmyWinningSerials);
        if (topLists.isEmpty()) {
            return null;
        }
        return topLists;
    }

    private void putBlockbustersDecade(Set<TopLists> topLists, Integer releaseYear,
                                       Double voteAverage, Integer voteCount, Double popularity,
                                       Integer budget, Long revenue) {
        final int decadeYear = Year.now().getValue() - TEN;
        if (releaseYear >= decadeYear && voteAverage >= RATING_LIMIT
                && voteCount >= VOTE_COUNT_LIMIT && popularity >= POPULARITY_LIMIT
                && revenue >= LIMIT_REVENUE) {
            final double profitabilityRatio = (double) revenue / budget;
            if (profitabilityRatio >= TWO) {
                topLists.add(TopLists.TOP_MOST_WATCHED_BLOCKBUSTERS_OF_THE_DECADE);
            }
        }
    }

    private void putBlockbustersDecadeForTvShows(Set<TopLists> topLists, Integer releaseYear,
                                       Double voteAverage, Integer voteCount, Double popularity) {
        final int decadeYear = Year.now().getValue() - TEN;
        if (releaseYear >= decadeYear && voteAverage >= RATING_LIMIT
                && voteCount >= VOTE_COUNT_LIMIT && popularity >= POPULARITY_LIMIT) {
            topLists.add(TopLists.TOP_MOST_WATCHED_BLOCKBUSTERS_OF_THE_DECADE);
        }
    }

    private void putOscarWinningMedia(Set<TopLists> topLists, String title,
                                      Set<String> oscarWinningMedia) {
        if (oscarWinningMedia.contains(title)) {
            topLists.add(TopLists.TOP_OSCAR_WINNING_MASTERPIECES);
            oscarWinningMedia.remove(title);
        }
    }

    private void putEmmyWinningTvSerials(Set<TopLists> topLists, String title,
                                         Set<String> emmyWinningSerials) {
        if (emmyWinningSerials.contains(title)) {
            topLists.add(TopLists.TOP_EMMY_WINNING_MASTERPIECES);
        }
    }

    private void putIconicMovies(Set<TopLists> topLists, Double voteAverage, Integer voteCount,
                                 Double popularity, Integer releaseYear) {
        if (releaseYear >= START_YEAR) {
            if (voteAverage >= RATING_LIMIT && voteCount >= VOTE_COUNT_LIMIT
                    && popularity >= POPULARITY_LIMIT) {
                topLists.add(TopLists.ICONIC_MOVIES_OF_THE_21ST_CENTURY);
            }
        }
    }

    void putSuperheroMovies(Set<TopLists> topLists, List<Keyword> keywords, Double voteAverage) {
        if (keywords.stream().map(keyword -> keyword.getName().equals(SUPERHERO))
                .findFirst().orElse(false) && voteAverage >= RATING_LIMIT) {
            topLists.add(TopLists.TOP_100_SUPERHERO_MOVIES);
        }
    }

    private void putTopRatedImdbMovies(Set<TopLists> topLists, Integer releaseYear,
                                       Double voteAverage, Integer voteCount) {
        final int previousYear = Year.now().getValue() - ONE;
        if (voteAverage >= IMDB_TOP_RATING_LIMIT
                && voteCount >= VOTE_COUNT_LIMIT && releaseYear <= (previousYear)) {
            topLists.add(TopLists.TOP_RATED_IMDB_MOVIES_OF_All_TIME);
        }
    }
}
