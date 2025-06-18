package org.cyberrealm.tech.muvio.service;

import java.util.Map;
import java.util.Set;
import org.cyberrealm.tech.muvio.model.Actor;
import org.cyberrealm.tech.muvio.model.LocalizationMedia;
import org.cyberrealm.tech.muvio.model.Media;

public interface MediaFactory {
    Media createMovie(
            Integer movieId, Set<String> moviesTop250, Set<String> oscarWinningMedia,
            Map<Integer, Actor> actors, Set<LocalizationMedia> localizationMediaStorage);

    Media createTvSerial(
            Integer seriesId, Set<String> serialsTop250, Set<String> emmyWinningMedia,
            Map<Integer, Actor> actors, Set<LocalizationMedia> localizationMediaStorage);
}
