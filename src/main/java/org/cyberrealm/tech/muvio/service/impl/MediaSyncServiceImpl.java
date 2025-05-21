package org.cyberrealm.tech.muvio.service.impl;

import static org.cyberrealm.tech.muvio.common.Constants.ONE;
import static org.cyberrealm.tech.muvio.common.Constants.TV;
import static org.cyberrealm.tech.muvio.common.Constants.ZERO;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.cyberrealm.tech.muvio.model.Actor;
import org.cyberrealm.tech.muvio.model.Media;
import org.cyberrealm.tech.muvio.service.MediaFactory;
import org.cyberrealm.tech.muvio.service.MediaSyncService;
import org.cyberrealm.tech.muvio.service.TmDbService;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MediaSyncServiceImpl implements MediaSyncService {
    private static final int LAST_PAGE = 500;
    private static final int FIRST_YEAR = 1946;
    private static final String EMPTY = "";
    private final TmDbService tmdbService;
    private final MediaFactory mediaFactory;

    @Override
    public void importMedia(String language, String region, int currentYear,
                            Set<String> imdbTop250, Set<String> winningMedia,
                            Map<Integer, Actor> actorStorage, Map<String, Media> mediaStorage,
                            boolean isMovies) {
        final Set<Integer> ids = IntStream.rangeClosed(ZERO, LAST_PAGE).parallel()
                .mapToObj(page -> isMovies
                        ? tmdbService.fetchPopularMovies(language, page, region)
                        : tmdbService.fetchPopularTvSerials(language, page))
                .flatMap(Collection::stream).filter(id -> isNewIds(id, isMovies, mediaStorage))
                .collect(Collectors.toSet());
        ids.parallelStream().forEach(id -> {
            final Media media = isMovies
                    ? mediaFactory.createMovie(language, id, imdbTop250,
                    winningMedia, actorStorage)
                    : mediaFactory.createTvSerial(language, id, imdbTop250,
                    winningMedia, actorStorage);
            if (media == null) {
              return;
            }
            mediaStorage.put(media.getId(), media);
        });
    }

    @Override
    public void importMediaByFilter(String language, int currentYear, Set<String> imdbTop250,
                                    Set<String> winningMedia, Map<String, Media> mediaStorage,
                                    Map<Integer, Actor> actorStorage, boolean isMovies) {
        final Set<Integer> ids =
                IntStream.rangeClosed(FIRST_YEAR, currentYear).parallel()
                        .boxed().flatMap(year -> IntStream.iterate(
                                        ONE, page -> page <= LAST_PAGE, page -> page + ONE)
                                .mapToObj(page -> (isMovies
                                        ? tmdbService.getFilteredMovies(year, page)
                                        : tmdbService.getFilteredTvShows(year, page)))
                                .takeWhile(set -> !set.isEmpty()))
                        .flatMap(Collection::stream)
                        .filter(id -> isNewIds(id, isMovies, mediaStorage))
                        .collect(Collectors.toSet());
        ids.parallelStream().forEach(id -> {
            final Media newMedia = isMovies
                    ? mediaFactory.createMovie(language, id, imdbTop250, winningMedia,
                    actorStorage)
                    : mediaFactory.createTvSerial(language, id, imdbTop250,
                    winningMedia, actorStorage);
            if (newMedia == null) {
                return;
            }
            mediaStorage.put(newMedia.getId(), newMedia);
        });
    }

    @Override
    public void importByFindingTitles(String language, String region, int currentYear,
                                      Map<Integer, Actor> actorStorage,
                                      Map<String, Media> mediaStorage, Set<String> imdbTop250,
                                      Set<String> winningMedia, boolean isMovies) {
        final Set<Integer> mediaId = new HashSet<>();
        findMediasIdsByTitles(language, region, imdbTop250, isMovies, mediaId);
        findMediasIdsByTitles(language, region, winningMedia, isMovies, mediaId);
        if (mediaId.isEmpty()) {
            return;
        }
        mediaId.parallelStream().filter(id -> isNewIds(id, isMovies, mediaStorage))
                .forEach(id -> {
                    final Media newMedia = isMovies
                            ? mediaFactory.createMovie(language, id, imdbTop250, winningMedia,
                            actorStorage)
                            : mediaFactory.createTvSerial(language, id, imdbTop250,
                            winningMedia, actorStorage);
                    if (newMedia == null) {
                        return;
                    }
                    mediaStorage.put(newMedia.getId(), newMedia);
                });
    }

    private void findMediasIdsByTitles(String language, String region, Set<String> titles,
                                       boolean isMovies, Set<Integer> mediaId) {
        if (!titles.isEmpty()) {
            titles.parallelStream()
                    .map(title -> isMovies
                            ? tmdbService.searchMovies(title, language, region)
                            : tmdbService.searchTvSeries(title, language))
                    .filter(Optional::isPresent)
                    .forEach(id -> mediaId.add(id.get()));
        }
    }

    private boolean isNewIds(int id, boolean isMovies, Map<String, Media> mediaStorage) {
        return !mediaStorage.containsKey((isMovies ? EMPTY : TV) + id);
    }
}
