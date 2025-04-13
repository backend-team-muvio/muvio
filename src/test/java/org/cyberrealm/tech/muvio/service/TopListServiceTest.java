package org.cyberrealm.tech.muvio.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cyberrealm.tech.muvio.common.Constants.TWO;

import info.movito.themoviedbapi.model.keywords.Keyword;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.cyberrealm.tech.muvio.model.TopLists;
import org.cyberrealm.tech.muvio.service.impl.TopListServiceImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TopListServiceTest {
    private static final List<Keyword> KEYWORDS = new ArrayList<>();
    private static final Keyword SUPERHERO = new Keyword();
    private static final double RATING = 9;
    private static final double POPULARITY = 5;
    private static final int VOTE_COUNT = 2000;
    private static final String TITLE = "Media1";
    private static final Set<String> WINNING_MEDIA = new HashSet<>();
    private static final long REVENUE = 90_000_000;
    private static final int BUDGET = 30_000_000;
    private static final TopListServiceImpl topListService = new TopListServiceImpl();
    private static int releaseYear;

    @BeforeAll
    static void beforeAll() {
        releaseYear = Year.now().getValue() - TWO;
        SUPERHERO.setName("superhero");
        KEYWORDS.add(SUPERHERO);
    }

    @BeforeEach
    void setUp() {
        WINNING_MEDIA.add(TITLE);
    }

    @Test
    public void putTopLists_ValidResponse_ReturnSet() {
        assertThat(topListService.putTopLists(KEYWORDS, RATING, VOTE_COUNT, POPULARITY,
                releaseYear, WINNING_MEDIA, TITLE, BUDGET, REVENUE)).containsExactlyInAnyOrder(
                TopLists.ICONIC_MOVIES_OF_THE_21ST_CENTURY,
                TopLists.TOP_OSCAR_WINNING_MASTERPIECES,
                TopLists.TOP_MOST_WATCHED_BLOCKBUSTERS_OF_THE_DECADE,
                TopLists.TOP_100_SUPERHERO_MOVIES, TopLists.TOP_RATED_IMDB_MOVIES_OF_All_TIME);
    }

    @Test
    public void putTopListsForTvShow_ValidResponse_ReturnSet() {
        assertThat(topListService.putTopListsForTvShow(KEYWORDS, RATING, VOTE_COUNT, POPULARITY,
                releaseYear, WINNING_MEDIA, TITLE)).containsExactlyInAnyOrder(
                TopLists.ICONIC_MOVIES_OF_THE_21ST_CENTURY, TopLists.TOP_EMMY_WINNING_MASTERPIECES,
                TopLists.TOP_MOST_WATCHED_BLOCKBUSTERS_OF_THE_DECADE,
                TopLists.TOP_100_SUPERHERO_MOVIES, TopLists.TOP_RATED_IMDB_MOVIES_OF_All_TIME);
    }
}
