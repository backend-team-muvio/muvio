package org.cyberrealm.tech.muvio.service.impl;

import static org.cyberrealm.tech.muvio.common.Constants.BACK_OFF;
import static org.cyberrealm.tech.muvio.common.Constants.ZERO;

import com.mongodb.MongoSocketReadTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.cyberrealm.tech.muvio.model.Actor;
import org.cyberrealm.tech.muvio.model.LocalizationMedia;
import org.cyberrealm.tech.muvio.model.Media;
import org.cyberrealm.tech.muvio.repository.ActorRepository;
import org.cyberrealm.tech.muvio.repository.LocalizationMediaRepository;
import org.cyberrealm.tech.muvio.repository.MediaRepository;
import org.cyberrealm.tech.muvio.service.MediaStorageService;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MediaStorageServiceImpl implements MediaStorageService {
    private static final int BATCH_SIZE = 500;
    private final MediaRepository mediaRepository;
    private final ActorRepository actorRepository;
    private final LocalizationMediaRepository localizationMediaRepository;

    @Override
    @Retryable(retryFor = {
            DataAccessResourceFailureException.class, MongoSocketReadTimeoutException.class
    },
            backoff = @Backoff(delay = BACK_OFF))
    public void deleteAll() {
        if (actorRepository != null) {
            actorRepository.deleteAll();
        }
        if (mediaRepository != null) {
            mediaRepository.deleteAll();
        }
        if (localizationMediaRepository != null) {
            localizationMediaRepository.deleteAll();
        }
    }

    @Override
    @Retryable(retryFor = {
            DataAccessResourceFailureException.class, MongoSocketReadTimeoutException.class
    },
            backoff = @Backoff(delay = BACK_OFF))
    public void saveAll(Map<Integer, Actor> actorStorage, Map<String, Media> mediaStorage,
                        Map<String, LocalizationMedia> localizationMediaStorage) {
        List<Actor> actorList = new ArrayList<>(actorStorage.values());
        for (int i = ZERO; i < actorList.size(); i += BATCH_SIZE) {
            int toIndex = Math.min(i + BATCH_SIZE, actorList.size());
            actorRepository.saveAll(actorList.subList(i, toIndex));
        }
        List<Media> mediaList = new ArrayList<>(mediaStorage.values());
        for (int i = ZERO; i < mediaList.size(); i += BATCH_SIZE) {
            int toIndex = Math.min(i + BATCH_SIZE, mediaList.size());
            mediaRepository.saveAll(mediaList.subList(i, toIndex));
        }
        List<LocalizationMedia> localizationMediaList = new ArrayList<>(localizationMediaStorage
                .values());
        for (int i = ZERO; i < localizationMediaList.size(); i += BATCH_SIZE) {
            int toIndex = Math.min(i + BATCH_SIZE, localizationMediaList.size());
            localizationMediaRepository.saveAll(localizationMediaList.subList(i, toIndex));
        }
        actorList.clear();
        mediaList.clear();
        localizationMediaList.clear();
    }
}
