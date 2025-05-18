package org.cyberrealm.tech.muvio.repository.impl;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.cyberrealm.tech.muvio.common.Constants.TEN;
import static org.cyberrealm.tech.muvio.common.Constants.ZERO;
import static org.cyberrealm.tech.muvio.util.TestConstants.STRING_1;
import static org.cyberrealm.tech.muvio.util.TestConstants.TITLE_1;
import static org.cyberrealm.tech.muvio.util.TestConstants.YEAR_2020;

import java.util.List;
import java.util.Set;
import org.cyberrealm.tech.muvio.config.AbstractMongoTest;
import org.cyberrealm.tech.muvio.dto.MediaGalleryRequestDto;
import org.cyberrealm.tech.muvio.dto.MediaVibeRequestDto;
import org.cyberrealm.tech.muvio.model.Category;
import org.cyberrealm.tech.muvio.model.GenreEntity;
import org.cyberrealm.tech.muvio.model.Media;
import org.cyberrealm.tech.muvio.model.Type;
import org.cyberrealm.tech.muvio.model.Vibe;
import org.cyberrealm.tech.muvio.repository.MediaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.PageRequest;

@DataMongoTest
public class MediaRepositoryCustomImplTest extends AbstractMongoTest {
    private static final String YEARS = "2010-2020";
    private static final String UNKNOWN = "unknown";
    @Autowired
    private MediaRepository mediaRepository;
    @Autowired
    private MediaRepositoryCustomImpl mediaRepositoryCustom;

    @BeforeEach
    void setUp() {
        mediaRepository.deleteAll();
        mediaRepository.save(getMedia());
    }

    @Test
    @DisplayName("Should return media by vibe, category, type and years")
    void getAllMediaByVibes_FilteredParams_ReturnListMediaVibeRequestDto() {
        final MediaVibeRequestDto request = new MediaVibeRequestDto(
                Vibe.BLOW_MY_MIND.name(), YEARS, Type.MOVIE.name(),
                Set.of(Category.BASED_ON_A_TRUE_STORY.name()), ZERO, TEN);
        final List<Media> actual = mediaRepositoryCustom.getAllMediaByVibes(request);
        assertThat(actual).isEqualTo(List.of(getMedia()));
    }

    @Test
    @DisplayName("Should ignore null/empty categories and still return by vibe")
    void getAllMediaByVibes_EmptyCategories_ReturnListMedia() {
        final MediaVibeRequestDto request = new MediaVibeRequestDto(
                Vibe.BLOW_MY_MIND.name(), YEARS, Type.MOVIE.name(), Set.of(), ZERO, TEN);
        final List<Media> actual = mediaRepositoryCustom.getAllMediaByVibes(request);
        assertThat(actual).isEqualTo(List.of(getMedia()));
    }

    @Test
    @DisplayName("Should return media by partial title, type, and years")
    void getAllForGallery_ValidParams_ReturnListMedia() {
        final MediaGalleryRequestDto request = new MediaGalleryRequestDto(
                TITLE_1, YEARS, Type.MOVIE.name());
        final List<Media> actual = mediaRepositoryCustom.getAllForGallery(
                request, PageRequest.of(ZERO, TEN));
        assertThat(actual).isEqualTo(List.of(getMedia()));
    }

    @Test
    @DisplayName("Should return empty list if title does not match")
    void getAllForGallery_TitleNoMatch_ReturnEmpty() {
        final MediaGalleryRequestDto request = new MediaGalleryRequestDto(
                UNKNOWN, YEARS, Type.MOVIE.name());
        final List<Media> actual = mediaRepositoryCustom
                .getAllForGallery(request, PageRequest.of(ZERO, TEN));
        assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("Should handle invalid or missing year range")
    void getAllMediaByVibes_EmptyYears_ReturnListMedia() {
        final MediaVibeRequestDto request = new MediaVibeRequestDto(
                Vibe.BLOW_MY_MIND.name(), null, Type.MOVIE.name(),
                Set.of(Category.BASED_ON_A_TRUE_STORY.name()), ZERO, TEN);
        final List<Media> actual = mediaRepositoryCustom.getAllMediaByVibes(request);
        assertThat(actual).isEqualTo(List.of(getMedia()));
    }

    private Media getMedia() {
        final Media media = new Media();
        media.setId(STRING_1);
        media.setTitle(TITLE_1);
        media.setType(Type.MOVIE);
        media.setGenres(Set.of(GenreEntity.SCIENCE_FICTION));
        media.setReleaseYear(YEAR_2020);
        media.setVibes(Set.of(Vibe.BLOW_MY_MIND));
        media.setCategories(Set.of(Category.BASED_ON_A_TRUE_STORY));
        return media;
    }
}
