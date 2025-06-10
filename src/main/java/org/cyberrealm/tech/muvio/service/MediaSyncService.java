package org.cyberrealm.tech.muvio.service;

import java.util.Map;
import java.util.Set;
import org.cyberrealm.tech.muvio.model.Actor;
import org.cyberrealm.tech.muvio.model.LocalizationMedia;
import org.cyberrealm.tech.muvio.model.Media;

public interface MediaSyncService {

    void importMedia(int currentYear, Set<String> imdbTop250,
                     Set<String> winningMedia, Map<Integer, Actor> actorStorage, Map<String,
                    Media> mediaStorage, Map<String, LocalizationMedia> localizationMediaStorage, boolean isMovies);

    void importByFindingTitles(int currentYear,
                               Map<Integer, Actor> actorStorage, Map<String, Media> mediaStorage,
                               Map<String, LocalizationMedia> localizationMediaStorage, Set<String> imdbTop250,
                               Set<String> winningMedia, boolean isMovies);

    void importMediaByFilter(int currentYear, Set<String> imdbTop250,
                             Set<String> winningMedia, Map<String, Media> mediaStorage,
                             Map<String, LocalizationMedia> localizationMediaStorage, Map<Integer, Actor> actorStorage, boolean isMovies);
}
