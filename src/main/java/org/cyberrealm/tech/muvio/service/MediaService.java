package org.cyberrealm.tech.muvio.service;

import java.util.Set;
import org.cyberrealm.tech.muvio.dto.MediaBaseDto;
import org.cyberrealm.tech.muvio.dto.MediaBaseDtoWithPoints;
import org.cyberrealm.tech.muvio.dto.MediaDto;
import org.cyberrealm.tech.muvio.dto.MediaDtoWithCast;
import org.cyberrealm.tech.muvio.dto.MediaGalleryRequestDto;
import org.cyberrealm.tech.muvio.dto.MediaVibeRequestDto;
import org.cyberrealm.tech.muvio.dto.PosterDto;
import org.cyberrealm.tech.muvio.dto.TitleDto;
import org.cyberrealm.tech.muvio.model.Media;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface MediaService {

    MediaDto getMediaById(String id);

    Media saveMedia(Media media);

    void deleteMediaById(String id);

    Media updateMedia(String id, Media updatedMedia);

    Slice<MediaBaseDtoWithPoints> getAllMediaByVibe(MediaVibeRequestDto requestDto,
                                                    Pageable pageable);

    Slice<MediaBaseDto> getAllForGallery(MediaGalleryRequestDto requestDto, Pageable pageable);

    Set<MediaDto> getAllLuck(int size);

    Slice<MediaBaseDto> getRecommendations(Pageable pageable);

    Slice<MediaDtoWithCast> findMediaByTopLists(String topList, Pageable pageable);

    Slice<PosterDto> findAllPosters(Pageable pageable);

    Slice<TitleDto> findAllTitles(Pageable pageable);

    MediaDto findByTitle(String title);
}
