package org.cyberrealm.tech.muvio.service.impl;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.cyberrealm.tech.muvio.common.Constants.ONE;
import static org.cyberrealm.tech.muvio.common.Constants.SIX;
import static org.cyberrealm.tech.muvio.common.Constants.TEN;
import static org.cyberrealm.tech.muvio.common.Constants.TITLE;
import static org.cyberrealm.tech.muvio.common.Constants.TRAILER;
import static org.cyberrealm.tech.muvio.common.Constants.ZERO;
import static org.cyberrealm.tech.muvio.util.TestConstants.DIRECTOR_NAME;
import static org.cyberrealm.tech.muvio.util.TestConstants.ID_STRING;
import static org.cyberrealm.tech.muvio.util.TestConstants.OVERVIEW;
import static org.cyberrealm.tech.muvio.util.TestConstants.POSTER_PATH;
import static org.cyberrealm.tech.muvio.util.TestConstants.STRING_1;
import static org.cyberrealm.tech.muvio.util.TestConstants.STRING_2;
import static org.cyberrealm.tech.muvio.util.TestConstants.STRING_3;
import static org.cyberrealm.tech.muvio.util.TestConstants.STRING_4;
import static org.cyberrealm.tech.muvio.util.TestConstants.STRING_5;
import static org.cyberrealm.tech.muvio.util.TestConstants.STRING_6;
import static org.cyberrealm.tech.muvio.util.TestConstants.TITLE_1;
import static org.cyberrealm.tech.muvio.util.TestConstants.TITLE_2;
import static org.cyberrealm.tech.muvio.util.TestConstants.TITLE_3;
import static org.cyberrealm.tech.muvio.util.TestConstants.TITLE_4;
import static org.cyberrealm.tech.muvio.util.TestConstants.TITLE_5;
import static org.cyberrealm.tech.muvio.util.TestConstants.TITLE_6;
import static org.cyberrealm.tech.muvio.util.TestConstants.VOTE_AVERAGE_8;
import static org.cyberrealm.tech.muvio.util.TestConstants.YEAR_2020;
import static org.cyberrealm.tech.muvio.util.TestUtil.PAGEABLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.cyberrealm.tech.muvio.dto.MediaBaseDto;
import org.cyberrealm.tech.muvio.dto.MediaDto;
import org.cyberrealm.tech.muvio.dto.MediaDtoFromDb;
import org.cyberrealm.tech.muvio.dto.MediaDtoWithCast;
import org.cyberrealm.tech.muvio.dto.MediaDtoWithCastFromDb;
import org.cyberrealm.tech.muvio.dto.MediaDtoWithPoints;
import org.cyberrealm.tech.muvio.dto.MediaGalleryRequestDto;
import org.cyberrealm.tech.muvio.dto.MediaVibeRequestDto;
import org.cyberrealm.tech.muvio.dto.PosterDto;
import org.cyberrealm.tech.muvio.dto.TitleDto;
import org.cyberrealm.tech.muvio.mapper.MediaMapper;
import org.cyberrealm.tech.muvio.model.Media;
import org.cyberrealm.tech.muvio.repository.MediaRepository;
import org.cyberrealm.tech.muvio.service.PaginationUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

@ExtendWith(MockitoExtension.class)
public class MediaServiceImplTest {
    private static final String COMEDY = "COMEDY";
    private static final String TYPE_MOVIE = "MOVIE";
    private static final String DURATION_90_STRING = "90";
    private static final int DURATION_90 = 90;
    private static final String VIBE = "vibe";
    private static final String YEAR_2020_STRING = "2020";
    private static final String TOP_LIST = "topList";
    private static final long COUNT = 3;

    @Mock
    private MediaRepository mediaRepository;
    @Mock
    private MediaMapper mediaMapper;
    @Mock
    private PaginationUtil paginationUtil;
    @InjectMocks
    private MediaServiceImpl mediaService;

    @Test
    @DisplayName("Verify getMediaById() method works")
    void getMediaById_validResponse_returnMediaDto() {
        final MediaDto expect = getMediaDto();
        when(mediaRepository.findMovieById(anyString()))
                .thenReturn(Optional.of(getMediaDtoFromDb()));
        when(mediaMapper.toMovieDto(any(MediaDtoFromDb.class))).thenReturn(expect);
        assertThat(mediaService.getMediaById(ID_STRING)).isEqualTo(expect);
    }

    @Test
    @DisplayName("Verify getAllMediaByVibe() method works")
    void getAllMediaByVibe_validResponse_returnSliceMediaDtoWithPoints() {
        final MediaDtoWithPoints mediaDtoWithPoints = new MediaDtoWithPoints(ID_STRING, TITLE,
                Set.of(COMEDY), VOTE_AVERAGE_8, TRAILER, POSTER_PATH, DURATION_90_STRING,
                DIRECTOR_NAME, Set.of(), List.of(), List.of(), YEAR_2020, OVERVIEW,
                TYPE_MOVIE, ONE);
        when(mediaRepository.getAllMediaByVibes(any(MediaVibeRequestDto.class)))
                .thenReturn(List.of(getMedia()));
        when(mediaMapper.toMediaDtoWithPoints(any(Media.class), any()))
                .thenReturn(mediaDtoWithPoints);
        when(paginationUtil.paginateList(any(), anyList())).thenReturn(
                new PageImpl<>(List.of(mediaDtoWithPoints)));
        final List<MediaDtoWithPoints> actual = mediaService.getAllMediaByVibe(
                new MediaVibeRequestDto(VIBE, null, null,
                null), getPageable()).getContent();
        assertThat(actual.getFirst()).isEqualTo(mediaDtoWithPoints);
        assertThat(actual.size()).isEqualTo(ONE);
    }

    @Test
    @DisplayName("Verify getAllForGallery() method works")
    void getAllForGallery_validResponse_returnSliceMediaBaseDto() {
        final MediaBaseDto mediaBaseDto = getMediaBaseDto();
        when(mediaRepository.getAllForGallery(any(MediaGalleryRequestDto.class), any()))
                .thenReturn(List.of(getMedia()));
        when(mediaMapper.toMediaBaseDto(any(Media.class))).thenReturn(mediaBaseDto);
        assertThat(mediaService.getAllForGallery(new MediaGalleryRequestDto(TITLE,
                YEAR_2020_STRING, TYPE_MOVIE), getPageable()).getContent())
                .isEqualTo(List.of(mediaBaseDto));
    }

    @Test
    @DisplayName("Verify getAllLuck() method works")
    void getAllLuck_validResponse_returnSetMediaDto() {
        final MediaDto mediaDto = getMediaDto();
        when(mediaRepository.getAllLuck(anyInt())).thenReturn(Set.of(getMediaDtoFromDb()));
        when(mediaMapper.toMovieDto(any(MediaDtoFromDb.class))).thenReturn(mediaDto);
        assertThat(mediaService.getAllLuck(ONE)).isEqualTo(Set.of(mediaDto));
    }

    @Test
    @DisplayName("Verify findMediaByTopLists() method works")
    void findMediaByTopLists_validResponse_returnSliceMediaDtoWithCast() {
        final MediaDtoWithCast mediaDtoWithCast = getMediaDtoWithCast();
        when(mediaRepository.findByTopListsContaining(anyString(), any()))
                .thenReturn(new SliceImpl<>(List.of(getMediaDtoWithCastFromDb())));
        when(mediaMapper.toMediaDtoWithCast(any(MediaDtoWithCastFromDb.class)))
                .thenReturn(mediaDtoWithCast);
        assertThat(mediaService.findMediaByTopLists(TOP_LIST, getPageable()).getContent())
                .isEqualTo(List.of(mediaDtoWithCast));
    }

    @Test
    @DisplayName("Verify findAllPosters() method works")
    void findAllPosters_validResponse_returnSlicePosterDto() {
        final Slice<PosterDto> slicePosterDto = getSlicePosterDto();
        when(mediaRepository.findAllPosters(any(Pageable.class))).thenReturn(slicePosterDto);
        assertThat(mediaService.findAllPosters(getPageable())).isEqualTo(slicePosterDto);
    }

    @Test
    @DisplayName("Verify findAllTitles() method works")
    void findAllTitles_validResponse_returnSliceTitleDto() {
        final Slice<TitleDto> titleDto = getSliceTitleDto();
        when(mediaRepository.findAllTitles(any(Pageable.class))).thenReturn(titleDto);
        assertThat(mediaService.findAllTitles(getPageable())).isEqualTo(titleDto);
    }

    @Test
    @DisplayName("Verify findByTitle() method works")
    void findByTitle_validResponse_returnMediaDto() {
        final MediaDto mediaDto = getMediaDto();
        when(mediaRepository.findByTitle(anyString())).thenReturn(getMediaDtoFromDb());
        when(mediaMapper.toMovieDto(any(MediaDtoFromDb.class))).thenReturn(mediaDto);
        assertThat(mediaService.findByTitle(TITLE)).isEqualTo(mediaDto);
    }

    @Test
    @DisplayName("Verify getAll() method works")
    void getAll_validResponse_returnListMediaBaseDto() {
        final List<MediaBaseDto> mediaBaseDto = List.of(getMediaBaseDto());
        when(mediaRepository.getAll(PAGEABLE)).thenReturn(mediaBaseDto);
        when(mediaMapper.toDuration(anyInt())).thenReturn(DURATION_90_STRING);
        assertThat(mediaService.getAll(PAGEABLE)).isEqualTo(mediaBaseDto);
    }

    @Test
    @DisplayName("Verify count() method works")
    void count_validResponse_returnLong() {
        when(mediaRepository.count()).thenReturn(COUNT);
        assertThat(mediaService.count()).isEqualTo(COUNT);
    }

    @Test
    @DisplayName("Verify getRecommendations() method works")
    void getRecommendations_validResponse_returnSliceMediaBaseDto() {
        when(mediaRepository.findMoviesByTypeGenreAndYears(
                any(), any(), anyInt(), any(Pageable.class))
        ).thenReturn(new PageImpl<>(getListMediaBaseDto()));
        when(paginationUtil.paginateList(any(PageRequest.class), anyList()))
                .thenReturn(new PageImpl<>(Arrays.asList(getListMediaBaseDto().toArray())));
        final Slice<MediaBaseDto> actual = mediaService.getRecommendations(ZERO);
        assertNotNull(actual);
        assertEquals(SIX, actual.getContent().size());
        assertEquals(TITLE_1, actual.getContent().getFirst().getTitle());
    }

    @Test
    @DisplayName("Return null for insufficient recommendations")
    void getRecommendations_nullResponse_returnNull() {
        MediaBaseDto media1 = new MediaBaseDto();
        media1.setId(STRING_1);
        media1.setTitle(TITLE_1);
        List<MediaBaseDto> shortList = List.of(media1);
        when(mediaRepository.findMoviesByTypeGenreAndYears(
                any(), any(), anyInt(), any(Pageable.class))
        ).thenReturn(new PageImpl<>(shortList));
        when(paginationUtil.paginateList(any(PageRequest.class), anyList()))
                .thenReturn(new PageImpl<>(List.of(media1)));
        Slice<MediaBaseDto> result = mediaService.getRecommendations(ZERO);
        assertNull(result);
    }

    private List<MediaBaseDto> getListMediaBaseDto() {
        final List<MediaBaseDto> list = new ArrayList<>();
        list.add(createMedia(STRING_1, TITLE_1));
        list.add(createMedia(STRING_2, TITLE_2));
        list.add(createMedia(STRING_3, TITLE_3));
        list.add(createMedia(STRING_4, TITLE_4));
        list.add(createMedia(STRING_5, TITLE_5));
        list.add(createMedia(STRING_6, TITLE_6));
        return list;
    }

    private MediaBaseDto createMedia(String id, String title) {
        MediaBaseDto media = new MediaBaseDto();
        media.setId(id);
        media.setTitle(title);
        return media;
    }

    private Media getMedia() {
        return new Media();
    }

    private Slice<TitleDto> getSliceTitleDto() {
        return new SliceImpl<>(List.of(new TitleDto(ID_STRING, TITLE)));
    }

    private Slice<PosterDto> getSlicePosterDto() {
        return new SliceImpl<>(List.of(new PosterDto(ID_STRING, POSTER_PATH)));
    }

    private Pageable getPageable() {
        return PageRequest.of(ZERO, TEN);
    }

    private MediaDtoWithCast getMediaDtoWithCast() {
        return new MediaDtoWithCast(ID_STRING, TITLE, Set.of(COMEDY), VOTE_AVERAGE_8,
                POSTER_PATH, DURATION_90_STRING, DIRECTOR_NAME, List.of());
    }

    private MediaDtoWithCastFromDb getMediaDtoWithCastFromDb() {
        return new MediaDtoWithCastFromDb(ID_STRING, TITLE, Set.of(COMEDY), VOTE_AVERAGE_8,
                POSTER_PATH, DURATION_90, DIRECTOR_NAME, List.of());
    }

    private MediaBaseDto getMediaBaseDto() {
        MediaBaseDto mediaBaseDto = new MediaBaseDto();
        mediaBaseDto.setId(ID_STRING);
        mediaBaseDto.setDuration(DURATION_90_STRING);
        return mediaBaseDto;
    }

    private MediaDtoFromDb getMediaDtoFromDb() {
        return new MediaDtoFromDb(ID_STRING, TITLE, Set.of(COMEDY), VOTE_AVERAGE_8,
                TRAILER, POSTER_PATH, DURATION_90, DIRECTOR_NAME, Set.of(), List.of(), List.of(),
                YEAR_2020, OVERVIEW, TYPE_MOVIE, Set.of());
    }

    private MediaDto getMediaDto() {
        return new MediaDto(ID_STRING, TITLE, Set.of(COMEDY), VOTE_AVERAGE_8,
                TRAILER, POSTER_PATH,DURATION_90_STRING, DIRECTOR_NAME, Set.of(), List.of(),
                List.of(), YEAR_2020, OVERVIEW, TYPE_MOVIE);
    }
}
