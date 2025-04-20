package org.cyberrealm.tech.muvio.service;

import static org.cyberrealm.tech.muvio.common.Constants.FOUR;
import static org.cyberrealm.tech.muvio.common.Constants.ID_STRING;
import static org.cyberrealm.tech.muvio.common.Constants.ONE;
import static org.cyberrealm.tech.muvio.common.Constants.SIX;
import static org.cyberrealm.tech.muvio.common.Constants.STRING_1;
import static org.cyberrealm.tech.muvio.common.Constants.STRING_2;
import static org.cyberrealm.tech.muvio.common.Constants.STRING_3;
import static org.cyberrealm.tech.muvio.common.Constants.STRING_4;
import static org.cyberrealm.tech.muvio.common.Constants.STRING_5;
import static org.cyberrealm.tech.muvio.common.Constants.STRING_6;
import static org.cyberrealm.tech.muvio.common.Constants.THREE;
import static org.cyberrealm.tech.muvio.common.Constants.TITLE;
import static org.cyberrealm.tech.muvio.common.Constants.TITLE_1;
import static org.cyberrealm.tech.muvio.common.Constants.TITLE_2;
import static org.cyberrealm.tech.muvio.common.Constants.TITLE_3;
import static org.cyberrealm.tech.muvio.common.Constants.TITLE_4;
import static org.cyberrealm.tech.muvio.common.Constants.TITLE_5;
import static org.cyberrealm.tech.muvio.common.Constants.TITLE_6;
import static org.cyberrealm.tech.muvio.common.Constants.TWO;
import static org.cyberrealm.tech.muvio.common.Constants.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.cyberrealm.tech.muvio.dto.MediaBaseDto;
import org.cyberrealm.tech.muvio.exception.EntityNotFoundException;
import org.cyberrealm.tech.muvio.service.impl.PaginationUtilImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PaginationUtilTest {
    private static final String NON_EXISTENT_FIELD = "nonExistentField";
    private final PaginationUtilImpl paginationUtil = new PaginationUtilImpl();

    @Test
    @DisplayName("Should return empty page when list is empty")
    void paginateList_emptyList_returnsEmptyPage() {
        final Pageable pageable = PageRequest.of(ZERO, FOUR);
        final Page<MediaBaseDto> actual = paginationUtil.paginateList(pageable, List.of());
        assertNotNull(actual);
        assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("Should return paged list without sorting")
    void paginateList_validPageable_returnsCorrectPage() {
        final Pageable pageable = PageRequest.of(ONE, THREE);
        final Page<MediaBaseDto> page = paginationUtil.paginateList(
                pageable, getMediaBaseDtoList());
        assertEquals(THREE, page.getContent().size());
        assertEquals(TITLE_4, page.getContent().getFirst().getTitle());
    }

    @Test
    @DisplayName("Should sort medias ascending by id")
    void paginateList_sortedAscendingById_returnsSortedPage() {
        final Pageable pageable = PageRequest.of(ZERO, SIX, Sort.by(ID_STRING).ascending());
        final Page<MediaBaseDto> page = paginationUtil.paginateList(
                pageable, getMediaBaseDtoList());
        final List<String> sortedIds = page.getContent().stream().map(MediaBaseDto::getId)
                .toList();
        assertEquals(List.of(STRING_1, STRING_2, STRING_3, STRING_4, STRING_5, STRING_6),
                sortedIds);
    }

    @Test
    @DisplayName("Should handle null fields during sorting")
    void paginateList_nullFieldsHandledProperly() {
        final List<MediaBaseDto> mediaBaseDtoList = List.of(createMediaBaseDto(STRING_1, null),
                createMediaBaseDto(STRING_2, TITLE_2), createMediaBaseDto(STRING_3, TITLE_3));
        final Pageable pageable = PageRequest.of(ZERO, THREE, Sort.by(TITLE).ascending());
        final Page<MediaBaseDto> page = paginationUtil.paginateList(pageable, mediaBaseDtoList);
        final List<String> actual = page.getContent().stream().map(MediaBaseDto::getTitle).toList();
        final List<String> expect = new ArrayList<>();
        expect.add(TITLE_2);
        expect.add(TITLE_3);
        expect.add(null);
        assertEquals(expect, actual);
    }

    @Test
    @DisplayName("Should throw exception for non-existing field")
    void paginateList_nonExistingField_throwsException() {
        List<MediaBaseDto> mediaBaseDtoList = List.of(createMediaBaseDto(STRING_1, TITLE_1),
                createMediaBaseDto(STRING_2, TITLE_2));
        Pageable pageable = PageRequest.of(ZERO, TWO, Sort.by(NON_EXISTENT_FIELD));
        assertThrows(EntityNotFoundException.class, () ->
                paginationUtil.paginateList(pageable, mediaBaseDtoList));
    }

    private MediaBaseDto createMediaBaseDto(String id, String title) {
        final MediaBaseDto mediaBaseDto = new MediaBaseDto();
        mediaBaseDto.setId(id);
        mediaBaseDto.setTitle(title);
        return mediaBaseDto;
    }

    private List<MediaBaseDto> getMediaBaseDtoList() {
        return List.of(createMediaBaseDto(STRING_1, TITLE_1),
                createMediaBaseDto(STRING_2, TITLE_2), createMediaBaseDto(STRING_3, TITLE_3),
                createMediaBaseDto(STRING_4, TITLE_4), createMediaBaseDto(STRING_5, TITLE_5),
                createMediaBaseDto(STRING_6, TITLE_6));
    }
}
