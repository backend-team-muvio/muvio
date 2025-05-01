package org.cyberrealm.tech.muvio.repository;

import static com.mongodb.assertions.Assertions.assertNotNull;
import static org.cyberrealm.tech.muvio.common.Constants.ONE;
import static org.cyberrealm.tech.muvio.common.Constants.TEN;
import static org.cyberrealm.tech.muvio.common.Constants.TWO;
import static org.cyberrealm.tech.muvio.common.Constants.ZERO;
import static org.cyberrealm.tech.muvio.util.TestConstants.STRING_1;
import static org.cyberrealm.tech.muvio.util.TestConstants.STRING_2;
import static org.cyberrealm.tech.muvio.util.TestConstants.TITLE_1;
import static org.cyberrealm.tech.muvio.util.TestConstants.TITLE_2;
import static org.cyberrealm.tech.muvio.util.TestConstants.VOTE_AVERAGE_8;
import static org.cyberrealm.tech.muvio.util.TestUtil.PAGEABLE;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.cyberrealm.tech.muvio.config.AbstractMongoTest;
import org.cyberrealm.tech.muvio.dto.MediaBaseDto;
import org.cyberrealm.tech.muvio.dto.MediaDtoFromDb;
import org.cyberrealm.tech.muvio.dto.PosterDto;
import org.cyberrealm.tech.muvio.dto.TitleDto;
import org.cyberrealm.tech.muvio.model.GenreEntity;
import org.cyberrealm.tech.muvio.model.Media;
import org.cyberrealm.tech.muvio.model.Type;
import org.cyberrealm.tech.muvio.repository.media.MediaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

@DataMongoTest
public class MediaRepositoryTest extends AbstractMongoTest {
    private static final String POSTER_PATH_1 = "posterPath 1";
    private static final String POSTER_PATH_2 = "posterPath 2";
    private static final int RELEASE_YEAR_2010 = 2010;
    private static final int RELEASE_YEAR_1999 = 1999;
    private static final int MIN_YEAR_2000 = 2000;
    @Autowired
    private MediaRepository mediaRepository;

    @BeforeEach
    void setUp() {
        mediaRepository.deleteAll();
        final Media media1 = new Media();
        media1.setId(STRING_1);
        media1.setTitle(TITLE_1);
        media1.setType(Type.MOVIE);
        media1.setGenres(Set.of(GenreEntity.SCIENCE_FICTION));
        media1.setRating(VOTE_AVERAGE_8);
        media1.setReleaseYear(RELEASE_YEAR_2010);
        media1.setPosterPath(POSTER_PATH_1);
        final Media media2 = new Media();
        media2.setId(STRING_2);
        media2.setTitle(TITLE_2);
        media2.setType(Type.MOVIE);
        media2.setGenres(Set.of(GenreEntity.SCIENCE_FICTION));
        media2.setRating(VOTE_AVERAGE_8);
        media2.setReleaseYear(RELEASE_YEAR_1999);
        media2.setPosterPath(POSTER_PATH_2);
        mediaRepository.saveAll(List.of(media1, media2));
    }

    @Test
    @DisplayName("Should return media by genre and year sorted by rating")
    void findMoviesByTypeGenreAndYears_ValidResponse_ReturnSliceMediaBaseDto() {
        final Slice<MediaBaseDto> actual = mediaRepository.findMoviesByTypeGenreAndYears(
                Type.MOVIE, GenreEntity.SCIENCE_FICTION, MIN_YEAR_2000, PageRequest.of(ZERO, TEN));
        assertEquals(ONE, actual.getContent().size());
        assertEquals(TITLE_1, actual.getContent().getFirst().getTitle());
    }

    @Test
    @DisplayName("Should return random N media with getAllLuck()")
    void getAllLuck_ValidResponse_ReturnSetMediaDtoFromDb() {
        final Set<MediaDtoFromDb> actual = mediaRepository.getAllLuck(ONE);
        assertEquals(ONE, actual.size());
    }

    @Test
    @DisplayName("Should return media by title")
    void findByTitle_ValidResponse_ReturnMediaDtoFromDb() {
        final MediaDtoFromDb actual = mediaRepository.findByTitle(TITLE_1);
        assertNotNull(actual);
        assertEquals(TITLE_1, actual.title());
    }

    @Test
    @DisplayName("Should return poster DTOs with findAllPosters()")
    void findAllPosters_ValidResponse_ReturnSlicePosterDto() {
        final Slice<PosterDto> actual = mediaRepository.findAllPosters(PageRequest.of(ZERO, TEN));
        assertEquals(actual.getContent(), List.of(new PosterDto(STRING_1, POSTER_PATH_1),
                new PosterDto(STRING_2, POSTER_PATH_2)));
    }

    @Test
    @DisplayName("Should return title DTOs with findAllTitles()")
    void findAllTitles_ValidResponse_ReturnSliceTitleDto() {
        final Slice<TitleDto> actual = mediaRepository.findAllTitles(PageRequest.of(ZERO, TEN));
        assertEquals(actual.getContent(), List.of(new TitleDto(STRING_1, TITLE_1),
                new TitleDto(STRING_2, TITLE_2)));
    }

    @Test
    @DisplayName("Should return all MediaBaseDto with getAll()")
    void getAll_ValidResponse_ReturnListMediaBaseDto() {
        final List<MediaBaseDto> actual = mediaRepository.getAll(PAGEABLE);
        assertEquals(TWO, actual.size());
        assertEquals(actual, getListMediaBaseDto());
    }

    private List<MediaBaseDto> getListMediaBaseDto() {
        final List<MediaBaseDto> list = new ArrayList<>();
        list.add(createMedia(STRING_1, TITLE_1, POSTER_PATH_1, RELEASE_YEAR_2010));
        list.add(createMedia(STRING_2, TITLE_2, POSTER_PATH_2, RELEASE_YEAR_1999));
        return list;
    }

    private MediaBaseDto createMedia(String id, String title, String posterPath, int releaseYear) {
        MediaBaseDto media = new MediaBaseDto();
        media.setId(id);
        media.setTitle(title);
        media.setGenres(Set.of(GenreEntity.SCIENCE_FICTION.name()));
        media.setRating(VOTE_AVERAGE_8);
        media.setPosterPath(posterPath);
        media.setReleaseYear(releaseYear);
        media.setType(Type.MOVIE.name());
        return media;
    }
}
