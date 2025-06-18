package org.cyberrealm.tech.muvio.service;

import java.util.Map;
import java.util.Set;
import org.cyberrealm.tech.muvio.model.Actor;
import org.cyberrealm.tech.muvio.model.LocalizationMedia;
import org.cyberrealm.tech.muvio.model.Media;

public interface MediaStorageService {
    void deleteAll();

    void saveAll(Map<Integer, Actor> actorStorage, Map<String, Media> mediaStorage,
                 Set<LocalizationMedia> localizationMediaStorage);
}
