package org.cyberrealm.tech.muvio.service.impl;

import static org.cyberrealm.tech.muvio.common.Constants.LANGUAGE_EN;
import static org.cyberrealm.tech.muvio.common.Constants.ONE;
import static org.cyberrealm.tech.muvio.common.Constants.REGION_US;

import java.time.Year;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.cyberrealm.tech.muvio.model.Actor;
import org.cyberrealm.tech.muvio.model.Media;
import org.cyberrealm.tech.muvio.service.AwardService;
import org.cyberrealm.tech.muvio.service.MediaStorageService;
import org.cyberrealm.tech.muvio.service.MediaSyncService;
import org.cyberrealm.tech.muvio.service.SyncSchedulerService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SyncSchedulerServiceImpl implements SyncSchedulerService {
    private boolean isRunning;
    private final AwardService awardService;
    private final MediaSyncService mediaSyncService;
    private final MediaStorageService mediaStorageService;

    @Override
    public void start() {
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
        isRunning = true;
    }

    @Scheduled(cron = "${sync.cron.time}")
    @Override
    public void worker() {
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
        isRunning = true;
    }

    @Override
    public void stop() {
        isRunning = false;
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public int getPhase() {
        return ONE;
    }
}
