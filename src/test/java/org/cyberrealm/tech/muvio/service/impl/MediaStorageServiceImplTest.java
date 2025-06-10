package org.cyberrealm.tech.muvio.service.impl;

import static org.cyberrealm.tech.muvio.common.Constants.THREE;
import static org.cyberrealm.tech.muvio.common.Constants.ZERO;
import static org.cyberrealm.tech.muvio.util.TestConstants.ACTOR_NAME_STRING;
import static org.cyberrealm.tech.muvio.util.TestConstants.EN_LANGUAGE;
import static org.cyberrealm.tech.muvio.util.TestConstants.MEDIA_TITLE;
import static org.cyberrealm.tech.muvio.util.TestConstants.TEST_SIZE;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.cyberrealm.tech.muvio.model.Actor;
import org.cyberrealm.tech.muvio.model.ActorName;
import org.cyberrealm.tech.muvio.model.Media;
import org.cyberrealm.tech.muvio.repository.ActorRepository;
import org.cyberrealm.tech.muvio.repository.MediaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MediaStorageServiceImplTest {
    @Mock
    private MediaRepository mediaRepository;
    @Mock
    private ActorRepository actorRepository;
    @InjectMocks
    private MediaStorageServiceImpl mediaStorageService;

    @Test
    @DisplayName("Verify deleteAll() method works")
    void deleteAll_validResponse_shouldCallDeleteAllOnRepositories() {
        mediaStorageService.deleteAll();
        verify(actorRepository).deleteAll();
        verify(mediaRepository).deleteAll();
    }

    @Test
    @DisplayName("Verify saveAll() method works")
    void saveAll_validResponse_shouldSaveEntitiesInBatches() {
        Map<Integer, Actor> actorStorage = new HashMap<>();
        Map<String, Media> mediaStorage = new HashMap<>();
        for (int i = ZERO; i < TEST_SIZE; i++) {
            Actor actor = new Actor();
            actor.setId(i);
            final ActorName actorName = new ActorName();
            actorName.setLang(EN_LANGUAGE);
            actorName.setName(ACTOR_NAME_STRING + i);
            actor.setNames(Set.of(actorName));
            actorStorage.put(i, actor);
        }
        for (int i = ZERO; i < TEST_SIZE; i++) {
            Media media = new Media();
            media.setId(String.valueOf(i));
            media.setTitle(MEDIA_TITLE + i);
            mediaStorage.put(String.valueOf(i), media);
        }
        mediaStorageService.saveAll(actorStorage, mediaStorage, localizationMediaStorage);
        verify(actorRepository, times(THREE)).saveAll(anyList());
        verify(mediaRepository, times(THREE)).saveAll(anyList());
    }
}
