package org.cyberrealm.tech.muvio.service;

import java.util.List;
import java.util.Set;
import org.cyberrealm.tech.muvio.dto.MediaBaseDto;
import org.cyberrealm.tech.muvio.dto.MediaDto;
import org.cyberrealm.tech.muvio.dto.MediaDtoFromDb;
import org.cyberrealm.tech.muvio.dto.MediaDtoWithCast;
import org.cyberrealm.tech.muvio.dto.MediaDtoWithCastFromDb;
import org.cyberrealm.tech.muvio.dto.MediaDtoWithPoints;
import org.cyberrealm.tech.muvio.dto.PosterDto;
import org.cyberrealm.tech.muvio.dto.TitleDto;
import org.cyberrealm.tech.muvio.model.Media;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface LocalizationMediaService {
    MediaDto findById(String id, Media media);

    Slice<MediaBaseDto> findByTitle(String title, Pageable pageable, String lang);

    Slice<MediaBaseDto> getAllForGalleryLocalization(
            List<Media> medias, String title, Pageable pageable, String lang);

    List<PosterDto> getRandomPosters(int size, String lang);

    List<MediaDtoWithPoints> getAllMediaByVibe(
            List<Media> mediaList, Set<String> categories, String lang);

    Set<MediaDto> getAllLuck(Set<MediaDtoFromDb> allLuck, String lang);

    Slice<MediaBaseDto> getRecommendations(
            Page<MediaBaseDto> mediaPage, PageRequest pageRequest, String lang);

    List<MediaDtoWithCast> findMediaByTopLists(Slice<MediaDtoWithCastFromDb> media, String lang);

    Slice<TitleDto> findAllTitles(Pageable pageable, String lang);

    List<MediaBaseDto> getAll(List<MediaBaseDto> mediaBaseList, String lang);
}
