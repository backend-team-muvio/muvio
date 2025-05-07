package org.cyberrealm.tech.muvio.repository;

import java.util.List;
import org.cyberrealm.tech.muvio.dto.MediaGalleryRequestDto;
import org.cyberrealm.tech.muvio.dto.MediaVibeRequestDto;
import org.cyberrealm.tech.muvio.model.Media;
import org.springframework.data.domain.Pageable;

public interface MediaRepositoryCustom {
    List<Media> getAllMediaByVibes(MediaVibeRequestDto query);

    List<Media> getAllForGallery(MediaGalleryRequestDto requestDto, Pageable pageable);
}
