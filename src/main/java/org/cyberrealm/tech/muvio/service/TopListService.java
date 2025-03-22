package org.cyberrealm.tech.muvio.service;

import info.movito.themoviedbapi.model.keywords.Keyword;
import java.util.List;
import java.util.Set;
import org.cyberrealm.tech.muvio.model.TopLists;

public interface TopListService {
    Set<TopLists> putTopLists(List<Keyword> keywords, Double voteAverage, Integer voteCount,
                              Double popularity, Integer releaseYear,
                              Set<String> oscarWinningMedia, String title, Integer budget,
                              Long revenue);

    Set<String> getOscarWinningMedia();

    Set<String> getEmmyWinningMedia();

    Set<TopLists> putTopListsForTvShow(List<Keyword> keywords, Double voteAverage,
                                       Integer voteCount, Double popularity, Integer releaseYear,
                                       Set<String> oscarWinningMedia, String title);
}
