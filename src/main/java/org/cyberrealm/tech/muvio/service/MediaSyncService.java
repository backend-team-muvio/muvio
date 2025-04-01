package org.cyberrealm.tech.muvio.service;

import java.util.Map;
import java.util.Set;
import org.cyberrealm.tech.muvio.model.Actor;
import org.cyberrealm.tech.muvio.model.Media;

public interface MediaSyncService {

    void importMedia(int fromPage, int toPage, String language, String location, int currentYear,
                     Set<String> imdbTop250, Set<String> winningMedia, Map<Integer,
                    Actor> actors, Map<String, Media> medias, boolean isMovies);

    void importByFindingTitles(String language, String region, int currentYear,
                               Map<Integer, Actor> actors, Map<String, Media> medias,
                               Set<String> imdbTop250,
                               Set<String> winningMedia, boolean isMovies);

    void importMediaByFilter(String language, int currentYear, Set<String> imdbTop250,
                             Set<String> winningMedia, Map<String, Media> media,
                             Map<Integer, Actor> actors, boolean isMovies);

    void deleteAll();

    void saveAll(Map<Integer, Actor> actors, Map<String, Media> medias);
}
