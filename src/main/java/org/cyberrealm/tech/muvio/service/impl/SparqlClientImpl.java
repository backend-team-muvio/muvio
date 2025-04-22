package org.cyberrealm.tech.muvio.service.impl;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import org.cyberrealm.tech.muvio.exception.NetworkRequestException;
import org.cyberrealm.tech.muvio.service.SparqlClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SparqlClientImpl implements SparqlClient {
    private static final String QUERY = "?query=";
    private static final String FORMAT_JSON = "&format=json";
    private static final String GET = "GET";
    private static final String PATTERN_A = "\\A";
    @Value("${sparql.endpoint}")
    private String sparqlEndpoint;

    @Override
    public String executeQuery(String query) {
        try {
            final String queryUrl = sparqlEndpoint + QUERY + URLEncoder
                    .encode(query, StandardCharsets.UTF_8) + FORMAT_JSON;
            final HttpURLConnection connection = (HttpURLConnection) new URI(queryUrl).toURL()
                    .openConnection();
            connection.setRequestMethod(GET);
            try (Scanner scanner = new Scanner(connection.getInputStream())) {
                return scanner.useDelimiter(PATTERN_A).next();
            }
        } catch (IOException | URISyntaxException e) {
            throw new NetworkRequestException("Error during SPARQL query execution", e);
        }
    }
}
