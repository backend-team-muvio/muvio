package org.cyberrealm.tech.muvio.service.impl;

import static org.cyberrealm.tech.muvio.common.Constants.ZERO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.cyberrealm.tech.muvio.model.Actor;
import org.cyberrealm.tech.muvio.model.Media;
import org.cyberrealm.tech.muvio.repository.actors.ActorRepository;
import org.cyberrealm.tech.muvio.repository.media.MediaRepository;
import org.cyberrealm.tech.muvio.service.MediaStorageService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MediaStorageServiceImpl implements MediaStorageService {
    private static final int BATCH_SIZE = 500;
    private final MediaRepository mediaRepository;
    private final ActorRepository actorRepository;

    @Override
    public void deleteAll() {
        if (actorRepository != null) {
            actorRepository.deleteAll();
        }
        if (mediaRepository != null) {
            mediaRepository.deleteAll();
        }
    }

    @Override
    public void saveAll(Map<Integer, Actor> actorStorage, Map<String, Media> mediaStorage) {
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
    }
}
