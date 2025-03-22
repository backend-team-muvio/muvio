package org.cyberrealm.tech.muvio.service;

import info.movito.themoviedbapi.model.keywords.Keyword;
import java.util.List;
import java.util.Set;
import org.cyberrealm.tech.muvio.model.Category;

public interface CategoryService {
    Set<Category> putCategories(String overview, List<Keyword> keywords, Double rating,
                                Integer voteCount, Double popularity, Set<String> imdbTop250,
                                String title);
}
