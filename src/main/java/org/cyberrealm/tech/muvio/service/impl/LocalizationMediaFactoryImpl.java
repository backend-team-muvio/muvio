package org.cyberrealm.tech.muvio.service.impl;

import com.sun.java.accessibility.util.Translator;
import info.movito.themoviedbapi.model.movies.Credits;
import info.movito.themoviedbapi.model.movies.Crew;
import lombok.RequiredArgsConstructor;
import org.cyberrealm.tech.muvio.mapper.LocalizationMediaMapper;
import org.cyberrealm.tech.muvio.model.Actor;
import org.cyberrealm.tech.muvio.model.LocalizationEntry;
import org.cyberrealm.tech.muvio.model.LocalizationMedia;
import org.cyberrealm.tech.muvio.model.Media;
import org.cyberrealm.tech.muvio.service.LocalizationMediaFactory;
import org.cyberrealm.tech.muvio.service.MediaFactory;
import org.cyberrealm.tech.muvio.service.TmDbService;
import org.cyberrealm.tech.muvio.service.TranslateService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import static org.cyberrealm.tech.muvio.common.Constants.TEN;

@Service
@RequiredArgsConstructor
public class LocalizationMediaFactoryImpl implements LocalizationMediaFactory {
    private final TmDbService tmDbService;
    private final LocalizationMediaMapper localizationMediaMapper;
    private final Map<String, LocalizationEntry> localizationEntryMap;
    private final TranslateService translateService;

    @Override
    public boolean createFromMovie(
            Media media,
            Map<String, LocalizationMedia> localizationMediaStorage, Map<Integer, Actor> actors) {
        return localizationEntryMap.values().stream().allMatch(localizationEntry -> {
            final String language = localizationEntry.getLang();
            final int mediaId = Integer.parseInt(media.getId());
            LocalizationMedia localizationMedia = localizationMediaMapper
                    .toLocalizationMedia(tmDbService.fetchMovieDetails(mediaId, language), localizationEntry);

            if (localizationMedia.getOverview() == null || localizationMedia.getOverview().length() <= TEN) {
                return false;
            }
            localizationMediaStorage.putIfAbsent(localizationMedia.getId(), localizationMedia);
            return true;
        });
    }
}
