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

    MediaDto getMediaById(String id, String lang);

    Slice<MediaDtoWithPoints> getAllMediaByVibe(MediaVibeRequestDto requestDto);

    Slice<MediaBaseDto> getAllForGallery(MediaGalleryRequestDto requestDto, Pageable pageable);

    Set<MediaDto> getAllLuck(int size, String lang);

    Slice<MediaBaseDto> getRecommendations(int page, String lang);

    Slice<MediaDtoWithCast> findMediaByTopLists(String topList, int page, int size, String lang);

    List<PosterDto> getRandomPosters(int size, String lang);

    Slice<TitleDto> findAllTitles(Pageable pageable, String lang);

    Slice<MediaBaseDto> findByTitle(String title, Pageable pageable, String lang);

    List<MediaBaseDto> getAll(Pageable pageable, String lang);

    long count();

    MainPageInfoDto getMainPageInfo();
}
