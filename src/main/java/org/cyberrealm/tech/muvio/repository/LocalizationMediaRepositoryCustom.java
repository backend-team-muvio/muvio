package org.cyberrealm.tech.muvio.repository;

import java.util.List;
import org.cyberrealm.tech.muvio.dto.TitleDto;
import org.cyberrealm.tech.muvio.model.LocalizationMedia;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface LocalizationMediaRepositoryCustom {

    List<LocalizationMedia> getAllForGalleryLocalization(List<String> mediaId, String title);

    Slice<TitleDto> findAllTitles(Pageable pageable, String lang);

    List<LocalizationMedia> findByTitle(String title, Pageable pageable, String lang);
}
