package org.cyberrealm.tech.muvio.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cyberrealm.tech.muvio.common.Constants.THREE;
import static org.cyberrealm.tech.muvio.util.TestConstants.MEDIA_1;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Set;
import lombok.SneakyThrows;
import org.cyberrealm.tech.muvio.exception.NetworkRequestException;
import org.cyberrealm.tech.muvio.service.SparqlClient;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class AwardServiceImplTest {
    private static final String MEDIA_2 = "Media2";
    private static final String MEDIA_3 = "Media3";
    private static final Set<String> MEDIA_TEST = Set.of(MEDIA_1, MEDIA_2, MEDIA_3);
    private static final String JSON_RESPONSE_MOVIES = """
                [
                  {"name": "Media1"},
                  {"name": "Media2"},
                  {"name": "Media3"}
                ]
                """;
    private static final String JSON_RESPONSE_TV_SHOWS = """
                [
                  {"Show Name": "Media1"},
                  {"Show Name": "Media2"},
                  {"Show Name": "Media3"}
                ]
                """;
    private static final String SPARQL_JSON = """
            {
                     "head" : {
                       "vars" : [ "awardWorkLabel" ]
                     },
                     "results" : {
                       "bindings" : [ {
                         "awardWorkLabel" : {
                           "xml:lang" : "en",
                           "type" : "literal",
                           "value" : "Media1"
                         }
                       }, {
                         "awardWorkLabel" : {
                           "xml:lang" : "en",
                           "type" : "literal",
                           "value" : "Media2"
                         }
                       }, {
                         "awardWorkLabel" : {
                           "xml:lang" : "en",
                           "type" : "literal",
                           "value" : "Media3"
                         }
                       }
                ]
              }
            }
            """;
    private static final String HTML = """
                <html>
                    <body>
                        <table class="wikitable">
                            <tr>
                                <td>
                                    <i title="Media1">Media1</i>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <i title="Media2">Media2</i>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <i title="Media3">Media3</i>
                                </td>
                            </tr>
                        </table>
                    </body>
                </html>
                """;
    private static final String TEST_URL = "https://test/test";
    private static final String TOP_250_MOVIE_URL = "top250MovieUrl";
    private static final String TOP_250_TV_SHOWS_URL = "top250TvShowUrl";
    private static final String SPARQL_QUERY = "sparqlQuery";
    private static final String EMMY_WINNING_URL = "emmyWinnersUrl";
    @Mock
    private HttpClient httpClient;
    @Mock
    private HttpResponse<String> response;
    @Mock
    private SparqlClient sparqlClient;
    @Mock
    private Connection connection;
    @InjectMocks
    private AwardServiceImpl awardService;

    @SneakyThrows
    @Test
    @DisplayName("Verify getImdbTop250Movies() method works")
    public void getImdbTop250Movies_ValidResponse_ReturnSetTitles() {
        ReflectionTestUtils.setField(awardService, TOP_250_MOVIE_URL, TEST_URL);
        when(response.body()).thenReturn(JSON_RESPONSE_MOVIES);
        when(httpClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString())))
                .thenReturn(response);
        assertThat(awardService.getImdbTop250Movies()).isEqualTo(MEDIA_TEST);
    }

    @SneakyThrows
    @Test
    @DisplayName("Verify getImdbTop250TvShows() method works")
    public void getImdbTop250TvShows_ValidResponse_ReturnSetTitles() {
        ReflectionTestUtils.setField(awardService, TOP_250_TV_SHOWS_URL, TEST_URL);
        when(response.body()).thenReturn(JSON_RESPONSE_TV_SHOWS);
        when(httpClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString())))
                .thenReturn(response);
        assertThat(awardService.getImdbTop250TvShows()).isEqualTo(MEDIA_TEST);
    }

    @SneakyThrows
    @Test
    @DisplayName("Verify getOscarWinningMovies() method works")
    void getOscarWinningMovies_ValidResponse_ReturnsSetTitles() {
        ReflectionTestUtils.setField(awardService, SPARQL_QUERY, TEST_URL);
        when(sparqlClient.executeQuery(anyString())).thenReturn(SPARQL_JSON);
        final Set<String> actual = awardService.getOscarWinningMovies();
        assertThat(actual).containsExactlyInAnyOrder(MEDIA_1, MEDIA_2, MEDIA_3);
        assertThat(actual.size()).isEqualTo(THREE);
    }

    @Test
    @DisplayName("Verify getEmmyWinningTvShows() method works")
    void getEmmyWinningTvShows_ValidResponse_ReturnsSetTitles() {
        ReflectionTestUtils.setField(awardService, EMMY_WINNING_URL, TEST_URL);
        final Document mockDocument = Jsoup.parse(HTML);
        try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class)) {
            jsoupMock.when(() -> Jsoup.connect(anyString())).thenReturn(connection);
            when(connection.get()).thenReturn(mockDocument);
            assertThat(awardService.getEmmyWinningTvShows()).isEqualTo(MEDIA_TEST);
        } catch (IOException e) {
            throw new NetworkRequestException("Error during Emmy winning request", e);
        }
    }
}
