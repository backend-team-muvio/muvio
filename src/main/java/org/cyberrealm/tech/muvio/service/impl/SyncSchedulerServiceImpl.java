package org.cyberrealm.tech.muvio.service.impl;

import static org.cyberrealm.tech.muvio.common.Constants.LANGUAGE_EN;
import static org.cyberrealm.tech.muvio.common.Constants.REGION_US;

import java.time.Year;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cyberrealm.tech.muvio.model.Actor;
import org.cyberrealm.tech.muvio.model.LocalizationMedia;
import org.cyberrealm.tech.muvio.model.Media;
import org.cyberrealm.tech.muvio.service.AwardService;
import org.cyberrealm.tech.muvio.service.MediaStorageService;
import org.cyberrealm.tech.muvio.service.MediaSyncService;
import org.cyberrealm.tech.muvio.service.SyncSchedulerService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncSchedulerServiceImpl implements SyncSchedulerService {
    private final AwardService awardService;
    private final MediaSyncService mediaSyncService;
    private final MediaStorageService mediaStorageService;

    @Scheduled(initialDelayString = "${sync.initial.cron.time}")
    @CacheEvict(value = "mediaStatistics", allEntries = true)
    @Override
    public void start() {
        log.info("Initiating the initial media synchronization");
        final Map<Integer, Actor> actorStorage = new ConcurrentHashMap<>();
        final Map<String, Media> mediaStorage = new ConcurrentHashMap<>();
        final Map<String, LocalizationMedia> localizationMediaStorage = new ConcurrentHashMap<>();
        final int currentYear = Year.now().getValue();
        final Set<String> imdbTop250Movies = awardService.getImdbTop250Movies();
        final Set<String> oscarWinningMovies = awardService.getOscarWinningMovies();
        final Set<String> imdbTop250TvShows = awardService.getImdbTop250TvShows();
        final Set<String> emmyWinningTvShows = awardService.getEmmyWinningTvShows();
        mediaSyncService.importMedia(currentYear, imdbTop250Movies,
                oscarWinningMovies, actorStorage, mediaStorage, localizationMediaStorage, true);
        mediaSyncService.importMedia(currentYear, imdbTop250TvShows,
                emmyWinningTvShows, actorStorage, mediaStorage, localizationMediaStorage, false);
        mediaStorageService.deleteAll();
        mediaStorageService.saveAll(actorStorage, mediaStorage, localizationMediaStorage);
        log.info("Initial media synchronization completed successfully");
    }

    @Scheduled(cron = "${sync.cron.time}")
    @CacheEvict(value = "mediaStatistics", allEntries = true)
    @Override
    public void worker() {
        log.info("Starting the weekly media update");
        final Map<Integer, Actor> actorStorage = new ConcurrentHashMap<>();
        final Map<String, Media> mediaStorage = new ConcurrentHashMap<>();
        final Map<String, LocalizationMedia> localizationMediaStorage = new ConcurrentHashMap<>();
        final int currentYear = Year.now().getValue();
        final Set<String> imdbTop250Movies = awardService.getImdbTop250Movies();
        final Set<String> oscarWinningMovies = awardService.getOscarWinningMovies();
        final Set<String> imdbTop250TvShows = awardService.getImdbTop250TvShows();
        final Set<String> emmyWinningTvShows = awardService.getEmmyWinningTvShows();
        mediaSyncService.importMedia(currentYear, imdbTop250Movies,
                oscarWinningMovies, actorStorage, mediaStorage, localizationMediaStorage, true);
        mediaSyncService.importMedia(currentYear, imdbTop250TvShows,
                emmyWinningTvShows, actorStorage, mediaStorage, localizationMediaStorage, false);
        mediaSyncService.importByFindingTitles(currentYear, actorStorage,
                mediaStorage, localizationMediaStorage, imdbTop250Movies, oscarWinningMovies, true);
        mediaSyncService.importByFindingTitles(currentYear, actorStorage,
                mediaStorage, localizationMediaStorage, imdbTop250TvShows, emmyWinningTvShows, false);
        mediaSyncService.importMediaByFilter(currentYear, imdbTop250Movies,
                oscarWinningMovies, mediaStorage, localizationMediaStorage, actorStorage, true);
        mediaSyncService.importMediaByFilter(currentYear, imdbTop250TvShows,
                emmyWinningTvShows, mediaStorage, localizationMediaStorage, actorStorage, false);
        mediaStorageService.deleteAll();
        mediaStorageService.saveAll(actorStorage, mediaStorage, localizationMediaStorage);
        log.info("Weekly media update completed successfully");
    }
}
