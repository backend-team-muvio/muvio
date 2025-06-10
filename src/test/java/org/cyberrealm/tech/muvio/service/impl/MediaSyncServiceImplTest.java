package org.cyberrealm.tech.muvio.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.cyberrealm.tech.muvio.util.TestConstants.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.cyberrealm.tech.muvio.model.Actor;
import org.cyberrealm.tech.muvio.model.Media;
import org.cyberrealm.tech.muvio.model.Type;
import org.cyberrealm.tech.muvio.service.MediaFactory;
import org.cyberrealm.tech.muvio.service.TmDbService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MediaSyncServiceImplTest {
    @Mock
    private TmDbService tmdbService;
    @Mock
    private MediaFactory mediaFactory;
    @InjectMocks
    private MediaSyncServiceImpl mediaSyncService;

    private Map<String, Media> mediaStorage;
    private Map<Integer, Actor> actorStorage;
    private Set<String> imdbTop250;
    private Set<String> winningMedia;

    @BeforeEach
    void setUp() {
        mediaStorage = new HashMap<>();
        actorStorage = new HashMap<>();
        imdbTop250 = new HashSet<>(Arrays.asList(FIRST_MEDIA_ID, SECOND_MEDIA_ID));
        winningMedia = new HashSet<>(List.of(THIRD_MEDIA_ID));
    }

    @Test
    @DisplayName("Import popular movies")
    void importMedia_WhenCalledWithMovieData_ShouldImportMovies() {
        // Given
        when(tmdbService.fetchPopularMovies(anyString(), anyInt(), anyString()))
                .thenReturn(Set.of(POPULAR_MOVIE_ID_ONE, POPULAR_MOVIE_ID_TWO));
        when(mediaFactory.createMovie(anyInt(), anySet(), anySet(), anyMap(), localizationMediaStorage))
                .thenAnswer(invocation -> {
                    int id = invocation.getArgument(FIRST_RECORD);
                    return getMedia(String.valueOf(id), MOVIE_PREFIX, Type.MOVIE);
                });

        // When
        mediaSyncService.importMedia(CURRENT_YEAR, imdbTop250, winningMedia,
                actorStorage, mediaStorage, localizationMediaStorage, IS_MOVIES);

        // Then
        assertThat(mediaStorage).hasSize(EXPECTED_SIZE_TWO);
        assertThat(mediaStorage.containsKey(MOVIE_KEY_ONE)).isTrue();
        assertThat(mediaStorage.containsKey(MOVIE_KEY_TWO)).isTrue();
        verify(mediaFactory, times(EXPECTED_SIZE_TWO))
                .createMovie(anyInt(), anySet(), anySet(), anyMap(), localizationMediaStorage);
    }

    @Test
    @DisplayName("Import popular TV shows")
    void importMedia_WhenCalledWithTvData_ShouldImportTvShows() {
        // Given
        when(tmdbService.fetchPopularTvSerials(anyString(), anyInt()))
                .thenReturn(Set.of(POPULAR_TV_ID_ONE, POPULAR_TV_ID_TWO));
        when(mediaFactory.createTvSerial(anyInt(), anySet(), anySet(), anyMap(), localizationMediaStorage))
                .thenAnswer(invocation -> {
                    int id = invocation.getArgument(FIRST_RECORD);
                    return getMedia(TV_PREFIX + id, TV_SHOW_TITLE_PREFIX, Type.TV_SHOW);
                });

        // When
        mediaSyncService.importMedia(CURRENT_YEAR, imdbTop250, winningMedia,
                actorStorage, mediaStorage, localizationMediaStorage, IS_TV_SHOW);

        // Then
        assertThat(mediaStorage).hasSize(EXPECTED_SIZE_TWO);
        assertThat(mediaStorage.containsKey(TV_MOVIE_KEY_ONE)).isTrue();
        assertThat(mediaStorage.containsKey(TV_MOVIE_KEY_TWO)).isTrue();
        verify(mediaFactory, times(EXPECTED_SIZE_TWO))
                .createTvSerial(anyInt(), anySet(), anySet(), anyMap(), localizationMediaStorage);
    }

    @Test
    @DisplayName("Import media by filter")
    void importMediaByFilter_WhenCalledWithMovieData_ShouldImportFilteredMovies() {
        // Given
        when(tmdbService.getFilteredMovies(anyInt(), anyInt()))
                .thenReturn(Set.of(FILTERED_MOVIE_ID_ONE, FILTERED_MOVIE_ID_TWO));
        when(mediaFactory.createMovie(anyInt(), anySet(), anySet(), anyMap(), localizationMediaStorage))
                .thenAnswer(invocation -> {
                    int id = invocation.getArgument(FIRST_RECORD);
                    return getMedia(String.valueOf(id), FILTERED_MOVIE_PREFIX, Type.MOVIE);
                });

        // When
        mediaSyncService.importMediaByFilter(CURRENT_YEAR, imdbTop250, winningMedia,
                mediaStorage, localizationMediaStorage, actorStorage, IS_MOVIES);

        // Then
        assertThat(mediaStorage).hasSize(EXPECTED_SIZE_TWO);
        assertThat(mediaStorage.containsKey(FILTERED_MOVIE_KEY_ONE)).isTrue();
        assertThat(mediaStorage.containsKey(FILTERED_MOVIE_KEY_TWO)).isTrue();
        verify(mediaFactory, times(EXPECTED_SIZE_TWO))
                .createMovie(anyInt(), anySet(), anySet(), anyMap(), localizationMediaStorage);
    }

    @Test
    @DisplayName("Import media by titles")
    void importByFindingTitles_WhenCalledWithValidTitle_ShouldImportMatchingMedia() {
        // Given
        when(tmdbService.searchMovies(anyString(), anyString(), anyString()))
                .thenReturn(Optional.of(SEARCH_MOVIE_ID));
        when(mediaFactory.createMovie(anyInt(), anySet(), anySet(), anyMap(), localizationMediaStorage))
                .thenAnswer(invocation -> {
                    int id = invocation.getArgument(FIRST_RECORD);
                    return getMedia(String.valueOf(id), TITLE_BASED_MOVIE_PREFIX, Type.MOVIE);
                });

        // When
        mediaSyncService.importByFindingTitles(CURRENT_YEAR, actorStorage,
                mediaStorage, localizationMediaStorage, imdbTop250, winningMedia, IS_MOVIES);

        // Then
        assertThat(mediaStorage).hasSize(EXPECTED_SIZE_ONE);
        assertThat(mediaStorage.containsKey(SEARCH_MOVIE_KEY)).isTrue();
        verify(tmdbService, times(EXPECTED_SIZE_THREE))
                .searchMovies(anyString(), anyString(), anyString());
        verify(mediaFactory, times(EXPECTED_SIZE_ONE))
                .createMovie(anyInt(), anySet(), anySet(), anyMap(), localizationMediaStorage);
    }

    @Test
    @DisplayName("Import popular movies with empty sets")
    void importMedia_WhenCalledWithEmptySets_ShouldNotImportAnyMedia() {
        // Given
        imdbTop250.clear();
        winningMedia.clear();

        // When
        mediaSyncService.importMedia(CURRENT_YEAR, imdbTop250,
                winningMedia, actorStorage, mediaStorage, localizationMediaStorage, IS_MOVIES);

        // Then
        assertThat(mediaStorage).isEmpty();
    }

    @Test
    @DisplayName("Import media by filter returns empty set")
    void importMediaByFilter_WhenNoFilteredMoviesFound_ShouldNotImportAnyMedia() {
        // Given
        when(tmdbService.getFilteredMovies(anyInt(), anyInt())).thenReturn(Set.of());

        // When
        mediaSyncService.importMediaByFilter(CURRENT_YEAR, imdbTop250, winningMedia,
                mediaStorage, localizationMediaStorage, actorStorage, IS_MOVIES);

        // Then
        assertThat(mediaStorage).isEmpty();
        verify(mediaFactory, times(ZERO_OF_RECORDS))
                .createMovie(anyInt(), anySet(), anySet(), anyMap(), localizationMediaStorage);
    }

    @Test
    @DisplayName("Import media by titles returns empty when no media found")
    void importByFindingTitles_WhenNoMediaFound_ShouldNotImportAnyMedia() {
        // Given
        when(tmdbService.searchMovies(anyString(), anyString(), anyString()))
                .thenReturn(Optional.empty());

        // When
        mediaSyncService.importByFindingTitles(CURRENT_YEAR, actorStorage,
                mediaStorage, localizationMediaStorage, imdbTop250, winningMedia, IS_MOVIES);

        // Then
        assertThat(mediaStorage).isEmpty();
        verify(tmdbService, times(EXPECTED_SIZE_THREE))
                .searchMovies(anyString(), anyString(), anyString());
        verify(mediaFactory, times(ZERO_OF_RECORDS))
                .createMovie(anyInt(), anySet(), anySet(), anyMap(), localizationMediaStorage);
    }

    @Test
    @DisplayName("Import popular movies throws exception when tmdbService fails")
    void importMedia_WhenTmdbServiceFails_ShouldThrowException() {
        // Given
        when(tmdbService.fetchPopularMovies(anyString(), anyInt(), anyString()))
                .thenThrow(new RuntimeException(SERVICE_UNAVAILABLE_MESSAGE));

        // Then
        assertThatThrownBy(() ->
                mediaSyncService.importMedia(CURRENT_YEAR, imdbTop250,
                        winningMedia, actorStorage, mediaStorage, localizationMediaStorage, IS_MOVIES))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(SERVICE_UNAVAILABLE_MESSAGE);
    }

    private Media getMedia(String id, String titlePrefics, Type type) {
        return new Media(
                id,
                titlePrefics + id,
                null, RELEASE_YEAR_2022, List.of(), null,
                null, null, FIRST_POPULAR_MEDIA_DURATION, EMPTY,
                type, Set.of(), Set.of(), List.of(),
                List.of(), Set.of(), Set.of(), Set.of());
    }
}
