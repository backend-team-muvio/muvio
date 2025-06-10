package org.cyberrealm.tech.muvio.service;

import org.cyberrealm.tech.muvio.model.Actor;
import org.cyberrealm.tech.muvio.model.LocalizationMedia;
import org.cyberrealm.tech.muvio.model.Media;

import java.util.Map;

public interface LocalizationMediaFactory {
    public boolean createFromMovie(Media media,
                                   Map<String, LocalizationMedia> localizationMediaStorage,
                                   Map<Integer, Actor> actors);
}
