package org.cyberrealm.tech.muvio.service;

import java.util.List;
import java.util.Set;
import org.cyberrealm.tech.muvio.dto.MainPageInfoDto;
import org.cyberrealm.tech.muvio.dto.MediaBaseDto;
import org.cyberrealm.tech.muvio.dto.MediaDto;
import org.cyberrealm.tech.muvio.dto.MediaDtoWithCast;
import org.cyberrealm.tech.muvio.dto.MediaDtoWithPoints;
import org.cyberrealm.tech.muvio.dto.MediaGalleryRequestDto;
import org.cyberrealm.tech.muvio.dto.MediaVibeRequestDto;
import org.cyberrealm.tech.muvio.dto.PosterDto;
import org.cyberrealm.tech.muvio.dto.TitleDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface MediaService {

    MediaDto getMediaById(String id);

    Slice<MediaDtoWithPoints> getAllMediaByVibe(MediaVibeRequestDto requestDto);

    Slice<MediaBaseDto> getAllForGallery(MediaGalleryRequestDto requestDto, Pageable pageable);

    Set<MediaDto> getAllLuck(int size);

    Slice<MediaBaseDto> getRecommendations(int page);

    Slice<MediaDtoWithCast> findMediaByTopLists(String topList, int page, int size);

    Slice<PosterDto> findAllPosters(Pageable pageable);

    Slice<TitleDto> findAllTitles(Pageable pageable);

    MediaDto findByTitle(String title);

    List<MediaBaseDto> getAll(Pageable pageable);

    long count();

    MainPageInfoDto getMainPageInfo();
}
