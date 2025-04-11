package org.cyberrealm.tech.muvio.service;

import java.util.Map;
import java.util.Set;
import org.cyberrealm.tech.muvio.model.Actor;
import org.cyberrealm.tech.muvio.model.Media;

public interface MediaFactory {
    Media createMovie(String language, Integer movieId, Set<String> moviesTop250,
                              Set<String> oscarWinningMedia, Map<Integer, Actor> actors);

    Media createTvSerial(String language, Integer seriesId, Set<String> serialsTop250,
                         Set<String> emmyWinningMedia, Map<Integer, Actor> actors);
}
