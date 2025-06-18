package org.cyberrealm.tech.muvio.service;

import java.util.Set;
import org.cyberrealm.tech.muvio.model.LocalizationMedia;

public interface LocalizationMediaFactory {
    boolean createFromMovie(
            Integer mediaId, Set<LocalizationMedia> localizationMediaStorage);

    boolean createFromSerial(
            Integer seriesId, Set<LocalizationMedia> localizationMediaStorage);
}
