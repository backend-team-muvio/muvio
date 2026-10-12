package org.cyberrealm.tech.muvio.service.impl;

import static org.cyberrealm.tech.muvio.common.Constants.TEN;

import info.movito.themoviedbapi.model.movies.MovieDb;
import info.movito.themoviedbapi.model.tv.series.TvSeriesDb;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.cyberrealm.tech.muvio.mapper.LocalizationMediaMapper;
import org.cyberrealm.tech.muvio.model.LocalizationEntry;
import org.cyberrealm.tech.muvio.model.LocalizationMedia;
import org.cyberrealm.tech.muvio.service.LocalizationMediaFactory;
import org.cyberrealm.tech.muvio.service.TmDbService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocalizationMediaFactoryImpl implements LocalizationMediaFactory {
    private final TmDbService tmDbService;
    private final LocalizationMediaMapper localizationMediaMapper;
    private final Set<LocalizationEntry> localizationEntrySet;

    @Override
    public boolean createFromMovie(
            Integer mediaId, Set<LocalizationMedia> localizationMediaStorage) {
        final Set<LocalizationMedia> tempStorage = new HashSet<>();
        final boolean allValid = localizationEntrySet.stream().allMatch(localizationEntry -> {
            final String language = localizationEntry.getLang();

            final MovieDb movieDb = tmDbService.fetchFullMovieDetails(mediaId, language);
            if (movieDb == null) {
                return false;
            }

            LocalizationMedia localizationMedia = localizationMediaMapper
                    .toLocalizationMovie(movieDb, localizationEntry);

            if (isInvalidOverview(localizationMedia.getOverview())) {
                return false;
            }

            localizationMedia.setTrailer(tmDbService.extractTrailer(movieDb.getVideos()));
            tempStorage.add(localizationMedia);
            return true;
        });
        return storeIfValid(allValid, localizationMediaStorage, tempStorage);
    }

    @Override
    public boolean createFromSerial(
            Integer seriesId, Set<LocalizationMedia> localizationMediaStorage) {
        final Set<LocalizationMedia> tempStorage = new HashSet<>();
        final boolean allValid = localizationEntrySet.stream().allMatch(localizationEntry -> {
            final String language = localizationEntry.getLang();

            final TvSeriesDb tvSeriesDb = tmDbService.fetchFullTvSeriesDetails(seriesId, language);
            if (tvSeriesDb == null) {
                return false;
            }

            LocalizationMedia localizationMedia = localizationMediaMapper
                    .toLocalizationTvSerial(tvSeriesDb, localizationEntry);

            if (isInvalidOverview(localizationMedia.getOverview())) {
                return false;
            }

            localizationMedia.setTrailer(tmDbService.extractTrailer(tvSeriesDb.getVideos()));
            tempStorage.add(localizationMedia);
            return true;
        });
        return storeIfValid(allValid, localizationMediaStorage, tempStorage);
    }

    private boolean isInvalidOverview(String overview) {
        return overview == null || overview.length() <= TEN;
    }

    private boolean storeIfValid(boolean allValid, Set<LocalizationMedia> mainStorage,
                                 Set<LocalizationMedia> tempStorage) {
        if (allValid) {
            mainStorage.addAll(tempStorage);
            return true;
        }
        return false;
    }
}
