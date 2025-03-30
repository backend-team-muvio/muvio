package org.cyberrealm.tech.muvio.service;

import java.util.Map;
import java.util.Set;
import org.cyberrealm.tech.muvio.model.Actor;
import org.cyberrealm.tech.muvio.model.Media;
import org.cyberrealm.tech.muvio.model.Type;

public interface MediaSyncService {

    void importMedia(Type type, int fromPage, int toPage, String language, String location,
                     Set<String> imdbTop250, Set<String> winningMedia, Map<Integer, Actor> actors,
                     Map<String, Media> media);

    void deleteAll();

    void saveAll(Map<Integer, Actor> actors, Map<String, Media> medias);
}
