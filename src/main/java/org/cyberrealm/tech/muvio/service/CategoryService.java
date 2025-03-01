package org.cyberrealm.tech.muvio.service;

import info.movito.themoviedbapi.model.movies.KeywordResults;
import java.util.Set;
import org.cyberrealm.tech.muvio.model.Category;

public interface CategoryService {
    Set<Category> getCategories(String overview, KeywordResults keywords, Double rating,
                                Integer voteCount, Double popularity, Set<String> imdbTop250,
                                String title);

    Set<String> getImdbTop250();
}
