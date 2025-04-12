package org.cyberrealm.tech.muvio.service.impl;

import static org.cyberrealm.tech.muvio.common.Constants.TITLE;
import static org.cyberrealm.tech.muvio.common.Constants.ZERO;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.cyberrealm.tech.muvio.exception.NetworkRequestException;
import org.cyberrealm.tech.muvio.service.AwardService;
import org.cyberrealm.tech.muvio.service.SparqlClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AwardServiceImpl implements AwardService {
    private static final String NAME = "name";
    private static final String SHOW_NAME = "Show Name";
    private static final String RESULTS = "results";
    private static final String BINDINGS = "bindings";
    private static final String AWARD_WORK_LABEL = "awardWorkLabel";
    private static final String VALUE = "value";
    private static final String SELECT_TABLE = "table.wikitable";
    private static final String ROWS_TR = "tr";
    private static final String ROWS_TD = "td";
    private static final String ROWS_I = "i";
    @Value("${top250.movie.url}")
    private String top250MovieUrl;
    @Value("${top250.tvShow.url}")
    private String top250TvShowUrl;
    @Value("${sparql.endpoint}")
    private String sparqlEndpoint;
    @Value("${sparql.query}")
    private String sparqlQuery;
    @Value("${emmy.winners.url}")
    private String emmyWinnersUrl;
    private final HttpClient httpClient;
    private final SparqlClient sparqlClient;

    @Override
    public Set<String> getImdbTop250Movies() {
        return getTopSet(top250MovieUrl, NAME, httpClient);
    }

    @Override
    public Set<String> getImdbTop250TvShows() {
        return getTopSet(top250TvShowUrl, SHOW_NAME, httpClient);
    }

    @Override
    public Set<String> getOscarWinningMovies() {
        final Set<String> oscarWorks = new HashSet<>();
        final JSONArray results = new JSONObject(sparqlClient.executeQuery(sparqlQuery))
                .getJSONObject(RESULTS).getJSONArray(BINDINGS);
        for (int i = ZERO; i < results.length(); i++) {
            final JSONObject filmObj = results.getJSONObject(i);
            if (filmObj.has(AWARD_WORK_LABEL)) {
                final JSONObject awardWorkLabel = filmObj.getJSONObject(AWARD_WORK_LABEL);
                if (awardWorkLabel.has(VALUE)) {
                    final String title = awardWorkLabel.getString(VALUE);
                    oscarWorks.add(title);
                }
            }
        }
        return oscarWorks;
    }

    @Override
    public Set<String> getEmmyWinningTvShows() {
        Set<String> winners = new HashSet<>();
        try {
            Document doc = Jsoup.connect(emmyWinnersUrl).get();
            Elements tables = doc.select(SELECT_TABLE);
            for (Element table : tables) {
                parseTableRows(table, winners);
            }
        } catch (IOException e) {
            throw new NetworkRequestException("Error during Emmy winning request", e);
        }
        return winners;
    }

    private void parseTableRows(Element table, Set<String> winners) {
        Elements rows = table.select(ROWS_TR);
        for (Element row : rows) {
            Elements columns = row.select(ROWS_TD);
            for (Element column : columns) {
                extractShowNames(column, winners);
            }
        }
    }

    private void extractShowNames(Element column, Set<String> winners) {
        Elements elements = column.select(ROWS_I);
        for (Element element : elements) {
            String showName = element.getElementsByAttribute(TITLE).text().trim();
            if (!showName.isEmpty()) {
                winners.add(showName);
            }
        }
    }

    private Set<String> getTopSet(String url, String fieldName, HttpClient client) {
        final Set<String> imdbTop250 = new HashSet<>();
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            final HttpResponse<String> response = client
                    .send(request, HttpResponse.BodyHandlers.ofString());
            final String json = response.body();
            final ObjectMapper mapper = new ObjectMapper();
            final List<Map<String, Object>> movies = mapper
                    .readValue(json, new TypeReference<>() {});
            for (Map<String, Object> movie : movies) {
                imdbTop250.add((String) movie.get(fieldName));
            }
        } catch (IOException | InterruptedException e) {
            throw new NetworkRequestException("Failed to fetch the IMDB Top 250 page", e);
        }
        return imdbTop250;
    }
}
