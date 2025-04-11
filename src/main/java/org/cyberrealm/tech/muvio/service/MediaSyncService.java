package org.cyberrealm.tech.muvio.service;

import java.util.Map;
import java.util.Set;
import org.cyberrealm.tech.muvio.model.Actor;
import org.cyberrealm.tech.muvio.model.Media;

public interface MediaSyncService {

    void importMedia(String language, String location, int currentYear, Set<String> imdbTop250,
                     Set<String> winningMedia, Map<Integer, Actor> actorStorage, Map<String,
                    Media> mediaStorage, boolean isMovies);

    void importByFindingTitles(String language, String region, int currentYear,
                               Map<Integer, Actor> actorStorage, Map<String, Media> mediaStorage,
                               Set<String> imdbTop250,
                               Set<String> winningMedia, boolean isMovies);

    void importMediaByFilter(String language, int currentYear, Set<String> imdbTop250,
                             Set<String> winningMedia, Map<String, Media> mediaStorage,
                             Map<Integer, Actor> actorStorage, boolean isMovies);
}
