package org.cyberrealm.tech.muvio.service;

import info.movito.themoviedbapi.model.movies.KeywordResults;
import java.util.Set;
import org.cyberrealm.tech.muvio.model.TopLists;

public interface TopListService {
    Set<TopLists> putTopLists(KeywordResults keywords, Double voteAverage, Integer voteCount,
                              Double popularity, Integer releaseYear,
                              Set<String> oscarWinningMedia, String title, Integer budget,
                              Long revenue);

    Set<String> getOscarWinningMedia();
}
