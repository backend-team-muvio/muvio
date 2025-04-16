package org.cyberrealm.tech.muvio.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cyberrealm.tech.muvio.common.Constants.MEDIA_1;
import static org.cyberrealm.tech.muvio.common.Constants.TRUE_STORY;

import info.movito.themoviedbapi.model.keywords.Keyword;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.cyberrealm.tech.muvio.model.Category;
import org.cyberrealm.tech.muvio.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CategoryServiceTest {
    private static final String OVERVIEW = "Film based on true story";
    private static final List<Keyword> KEYWORDS = new ArrayList<>();
    private static final Keyword KEYWORD = new Keyword();
    private static final double RATING = 9;
    private static final double POPULARITY = 5;
    private static final int VOTE_COUNT = 2000;
    private static final Set<String> IMDB_TOP_250 = new HashSet<>();
    private static final CategoryServiceImpl categoryService = new CategoryServiceImpl();

    @BeforeAll
    static void beforeAll() {
        KEYWORD.setName(TRUE_STORY);
        KEYWORDS.add(KEYWORD);
        IMDB_TOP_250.add(MEDIA_1);
    }

    @Test
    @DisplayName("Verify putCategories() method works")
    public void putCategories_ValidResponse_ReturnSetCategories() {
        assertThat(categoryService.putCategories(OVERVIEW, KEYWORDS, RATING, VOTE_COUNT,
                POPULARITY, IMDB_TOP_250, MEDIA_1)).containsExactlyInAnyOrder(
                        Category.BASED_ON_A_TRUE_STORY, Category.IMD_TOP_250,
                Category.MUST_WATCH_LIST);
    }
}
