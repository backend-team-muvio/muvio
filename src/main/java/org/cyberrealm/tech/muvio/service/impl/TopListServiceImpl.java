package org.cyberrealm.tech.muvio.service.impl;

import info.movito.themoviedbapi.model.movies.KeywordResults;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import org.cyberrealm.tech.muvio.exception.NetworkRequestException;
import org.cyberrealm.tech.muvio.model.TopLists;
import org.cyberrealm.tech.muvio.service.TopListService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class TopListServiceImpl implements TopListService {
    private static final String SUPERHERO = "superhero";
    private static final int START_YEAR = 2001;
    private static final int POPULARITY_LIMIT = 4;
    private static final int VOTE_COUNT_LIMIT = 1000;
    private static final int RATING_LIMIT = 7;
    private static final String SPARQL_ENDPOINT = "https://query.wikidata.org/sparql";
    private static final String SPARQL_QUERY =
            """
                    SELECT DISTINCT ?awardWorkLabel
                    WHERE {
                      {
                        SELECT ?awardWork
                        WHERE {
                          ?award wdt:P31 wd:Q19020 .
                    
                          # Work that has been awarded an Oscar
                          {
                            ?awardWork wdt:P31 wd:Q11424 .
                            ?awardWork p:P166 ?awardStat .
                            ?awardStat ps:P166 ?award .
                          }
                          UNION {
                            ?awardWork wdt:P31 wd:Q5398426 .
                            ?awardWork p:P166 ?awardStat .
                            ?awardStat ps:P166 ?award .
                          }
                          UNION {
                            ?awardWork wdt:P31 wd:Q93204 .
                            ?awardWork p:P166 ?awardStat .
                            ?awardStat ps:P166 ?award .
                          }
                    
                          OPTIONAL {
                            ?awardWork rdfs:label ?awardWorkLabel .
                            FILTER (lang(?awardWorkLabel) = "en")
                          }
                        }
                      }
                    
                      SERVICE wikibase:label {
                        bd:serviceParam wikibase:language "en" .
                      }
                    }
                    ORDER BY ?awardWorkLabel
                    
                    """;
    private static final String RESULTS = "results";
    private static final String BINDINGS = "bindings";
    private static final String AWARD_WORK_LABEL = "awardWorkLabel";
    private static final String VALUE = "value";
    private static final int ZERO = 0;
    private static final String QUERY = "?query=";
    private static final String FORMAT_JSON = "&format=json";
    private static final String GET = "GET";
    private static final String PATTERN_A = "\\A";
    private static final int CURRENT_YEAR = Year.now().getValue();
    private static final int DECADE_YEAR;
    private static final int LIMIT_REVENUE = 50_000_000;
    private static final int TWO = 2;
    private static final double IMDB_TOP_RATING_LIMIT = 8.0;
    private static final int ONE = 1;

    static {
        DECADE_YEAR = CURRENT_YEAR - 10;
    }

    @Override
    public Set<TopLists> putTopLists(KeywordResults keywords, Double voteAverage,
                                     Integer voteCount, Double popularity, Integer releaseYear,
                                     Set<String> oscarWinningMedia, String title, Integer budget,
                                     Long revenue) {
        Set<TopLists> topLists = new HashSet<>();
        putSuperheroMovies(topLists, keywords);
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
    public Set<String> getOscarWinningMedia() {
        final Set<String> oscarWorks = new HashSet<>();
        final JSONArray results = new JSONObject(executeSparqlQuery()).getJSONObject(RESULTS)
                .getJSONArray(BINDINGS);
        for (int i = ZERO; i < results.length(); i++) {
            final JSONObject filmObj = results.getJSONObject(i);
            if (filmObj.has(AWARD_WORK_LABEL)) {
                final JSONObject awardWorkLabel = filmObj.getJSONObject(AWARD_WORK_LABEL);
                if (awardWorkLabel != null && awardWorkLabel.has(VALUE)) {
                    final String title = awardWorkLabel.getString(VALUE);
                    oscarWorks.add(title);
                }
            }
        }
        return oscarWorks;
    }

    private String executeSparqlQuery() {
        try {
            final String queryUrl = SPARQL_ENDPOINT + QUERY + URLEncoder.encode(SPARQL_QUERY,
                    StandardCharsets.UTF_8) + FORMAT_JSON;
            final HttpURLConnection connection = (HttpURLConnection) new URI(queryUrl)
                    .toURL().openConnection();
            connection.setRequestMethod(GET);
            try (Scanner scanner = new Scanner(connection.getInputStream())) {
                return scanner.useDelimiter(PATTERN_A).next();
            }
        } catch (IOException | URISyntaxException e) {
            throw new NetworkRequestException("Error during SPARQL query execution", e);
        }
    }

    private void putBlockbustersDecade(Set<TopLists> topLists, Integer releaseYear,
                                       Double voteAverage, Integer voteCount, Double popularity,
                                       Integer budget, Long revenue) {
        if (releaseYear >= DECADE_YEAR && voteAverage >= RATING_LIMIT
                && voteCount >= VOTE_COUNT_LIMIT && popularity >= POPULARITY_LIMIT
                && revenue >= LIMIT_REVENUE) {
            final double profitabilityRatio = (double) revenue / budget;
            if (profitabilityRatio >= TWO) {
                topLists.add(TopLists.TOP_MOST_WATCHED_BLOCKBUSTERS_OF_THE_DECADE);
            }
        }
    }

    private void putOscarWinningMedia(Set<TopLists> topLists, String title,
                                      Set<String> oscarWinningMedia) {
        if (oscarWinningMedia.contains(title)) {
            topLists.add(TopLists.TOP_OSCAR_WINNING_MASTERPIECES);
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

    void putSuperheroMovies(Set<TopLists> topLists, KeywordResults keywords) {
        if (keywords.getKeywords().stream().map(keyword -> keyword.getName().equals(SUPERHERO))
                .findFirst().orElse(false)) {
            topLists.add(TopLists.TOP_100_SUPERHERO_MOVIES);
        }
    }

    private void putTopRatedImdbMovies(Set<TopLists> topLists, Integer releaseYear,
                                       Double voteAverage, Integer voteCount) {
        if (voteAverage >= IMDB_TOP_RATING_LIMIT
                && voteCount >= VOTE_COUNT_LIMIT && releaseYear <= (CURRENT_YEAR - ONE)) {
            topLists.add(TopLists.TOP_RATED_IMDB_MOVIES_OF_All_TIME);
        }
    }
}
