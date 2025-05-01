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
import org.cyberrealm.tech.muvio.model.Media;
import org.cyberrealm.tech.muvio.service.AwardService;
import org.cyberrealm.tech.muvio.service.MediaStorageService;
import org.cyberrealm.tech.muvio.service.MediaSyncService;
import org.cyberrealm.tech.muvio.service.SyncSchedulerService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncSchedulerServiceImpl implements SyncSchedulerService {
    private final AwardService awardService;
    private final MediaSyncService mediaSyncService;
    private final MediaStorageService mediaStorageService;

    @Scheduled(initialDelay = 500)
    @Override
    public void start() {
        log.info("Initiating the initial media synchronization");
        final Map<Integer, Actor> actorStorage = new ConcurrentHashMap<>();
        final Map<String, Media> mediaStorage = new ConcurrentHashMap<>();
        final int currentYear = Year.now().getValue();
        final Set<String> imdbTop250Movies = awardService.getImdbTop250Movies();
        final Set<String> oscarWinningMovies = awardService.getOscarWinningMovies();
        final Set<String> imdbTop250TvShows = awardService.getImdbTop250TvShows();
        final Set<String> emmyWinningTvShows = awardService.getEmmyWinningTvShows();
        mediaSyncService.importMedia(LANGUAGE_EN, REGION_US, currentYear, imdbTop250Movies,
                oscarWinningMovies, actorStorage, mediaStorage, true);
        mediaSyncService.importMedia(LANGUAGE_EN, REGION_US, currentYear, imdbTop250TvShows,
                emmyWinningTvShows, actorStorage, mediaStorage, false);
        mediaStorageService.deleteAll();
        mediaStorageService.saveAll(actorStorage, mediaStorage);
        log.info("Initial media synchronization completed successfully");
    }

    @Scheduled(cron = "${sync.cron.time}")
    @Override
    public void worker() {
        log.info("Starting the weekly media update");
        final Map<Integer, Actor> actorStorage = new ConcurrentHashMap<>();
        final Map<String, Media> mediaStorage = new ConcurrentHashMap<>();
        final int currentYear = Year.now().getValue();
        final Set<String> imdbTop250Movies = awardService.getImdbTop250Movies();
        final Set<String> oscarWinningMovies = awardService.getOscarWinningMovies();
        final Set<String> imdbTop250TvShows = awardService.getImdbTop250TvShows();
        final Set<String> emmyWinningTvShows = awardService.getEmmyWinningTvShows();
        mediaSyncService.importMedia(LANGUAGE_EN, REGION_US, currentYear, imdbTop250Movies,
                oscarWinningMovies, actorStorage, mediaStorage, true);
        mediaSyncService.importMedia(LANGUAGE_EN, REGION_US, currentYear, imdbTop250TvShows,
                emmyWinningTvShows, actorStorage, mediaStorage, false);
        mediaSyncService.importByFindingTitles(LANGUAGE_EN, REGION_US, currentYear, actorStorage,
                mediaStorage, imdbTop250Movies, oscarWinningMovies, true);
        mediaSyncService.importByFindingTitles(LANGUAGE_EN, REGION_US, currentYear, actorStorage,
                mediaStorage, imdbTop250TvShows, emmyWinningTvShows, false);
        mediaSyncService.importMediaByFilter(LANGUAGE_EN, currentYear, imdbTop250Movies,
                oscarWinningMovies, mediaStorage, actorStorage, true);
        mediaSyncService.importMediaByFilter(LANGUAGE_EN, currentYear, imdbTop250TvShows,
                emmyWinningTvShows, mediaStorage, actorStorage, false);
        mediaStorageService.deleteAll();
        mediaStorageService.saveAll(actorStorage, mediaStorage);
        log.info("Weekly media update completed successfully");
    }
}
