package org.cyberrealm.tech.muvio.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cyberrealm.tech.muvio.util.TestConstants.CONTENT_STRING;
import static org.cyberrealm.tech.muvio.util.TestConstants.CRITERIA_YEARS_PERIOD;
import static org.cyberrealm.tech.muvio.util.TestConstants.EMPTY;
import static org.cyberrealm.tech.muvio.util.TestConstants.EMPTY_COUNT;
import static org.cyberrealm.tech.muvio.util.TestConstants.EXPECTED_COUNT;
import static org.cyberrealm.tech.muvio.util.TestConstants.FIRST_MEDIA_ID;
import static org.cyberrealm.tech.muvio.util.TestConstants.FIRST_MEDIA_TITLE;
import static org.cyberrealm.tech.muvio.util.TestConstants.FIRST_MOVIE_TITLE;
import static org.cyberrealm.tech.muvio.util.TestConstants.FIRST_RECORD;
import static org.cyberrealm.tech.muvio.util.TestConstants.INVALID_CATEGORIES;
import static org.cyberrealm.tech.muvio.util.TestConstants.INVALID_MEDIA_ID;
import static org.cyberrealm.tech.muvio.util.TestConstants.INVALID_YEARS_PERIOD;
import static org.cyberrealm.tech.muvio.util.TestConstants.NONEXISTENT_TITLE;
import static org.cyberrealm.tech.muvio.util.TestConstants.NON_EXISTENT_VIBE;
import static org.cyberrealm.tech.muvio.util.TestConstants.PAGE_NUMBER_ZERO;
import static org.cyberrealm.tech.muvio.util.TestConstants.PAGE_SIZE_FIVE;
import static org.cyberrealm.tech.muvio.util.TestConstants.PARAM_NAME_CATEGORIES;
import static org.cyberrealm.tech.muvio.util.TestConstants.PARAM_NAME_PAGE;
import static org.cyberrealm.tech.muvio.util.TestConstants.PARAM_NAME_SIZE;
import static org.cyberrealm.tech.muvio.util.TestConstants.PARAM_NAME_TITLE;
import static org.cyberrealm.tech.muvio.util.TestConstants.PARAM_NAME_TYPE;
import static org.cyberrealm.tech.muvio.util.TestConstants.PARAM_NAME_VIBE;
import static org.cyberrealm.tech.muvio.util.TestConstants.PARAM_NAME_YEARS;
import static org.cyberrealm.tech.muvio.util.TestConstants.POPULAR_MOVIE_ID_ONE;
import static org.cyberrealm.tech.muvio.util.TestConstants.ZERO_OF_RECORDS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Set;
import org.cyberrealm.tech.muvio.config.AbstractMongoTest;
import org.cyberrealm.tech.muvio.dto.MediaBaseDto;
import org.cyberrealm.tech.muvio.dto.MediaDto;
import org.cyberrealm.tech.muvio.model.Media;
import org.cyberrealm.tech.muvio.model.TopLists;
import org.cyberrealm.tech.muvio.model.Type;
import org.cyberrealm.tech.muvio.model.Vibe;
import org.cyberrealm.tech.muvio.repository.MediaRepository;
import org.cyberrealm.tech.muvio.util.TestUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class MediaControllerTest extends AbstractMongoTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MediaRepository mediaRepository;

    @BeforeEach
    void setUp() {
        mediaRepository.deleteAll();

        Media firstMedia = TestUtil.firstMedia;
        Media secondMedia = TestUtil.secondMedia;

        mediaRepository.saveAll(List.of(firstMedia, secondMedia));
    }

    @AfterEach
    void tearDown() {
        mediaRepository.deleteAll();
    }

    @Test
    @DisplayName("get Media by id with existing id")
    void getMediaById_ExistingId_ReturnsMedia() throws Exception {
        // When
        MvcResult mvcResult = mockMvc.perform(get("/media/{id}", FIRST_MEDIA_ID))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String jsonResponse = mvcResult.getResponse().getContentAsString();
        MediaDto responseDto = objectMapper.readValue(jsonResponse, MediaDto.class);
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.id()).isEqualTo(FIRST_MEDIA_ID);
    }

    @Test
    @DisplayName("get Media by id with nonexistent id")
    void getMediaById_NonExistingId_ReturnsNotFound() throws Exception {
        // When
        mockMvc.perform(get("/media/{id}", INVALID_MEDIA_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("get all Media")
    void getAllMedia_WhenRepositoryNotEmpty_ReturnsMediaList() throws Exception {
        // When
        MvcResult mvcResult = mockMvc.perform(get("/media/all"))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String jsonResponse = mvcResult.getResponse().getContentAsString();
        List<MediaBaseDto> responseDtos = objectMapper.readValue(jsonResponse,
                new TypeReference<>() {
                });
        assertThat(responseDtos).isNotEmpty();
    }

    @Test
    @DisplayName("get all Media when repository empty")
    void getAllMedia_WhenRepositoryEmpty_ReturnsEmptyList() throws Exception {
        // When
        mediaRepository.deleteAll();
        MvcResult mvcResult = mockMvc.perform(get("/media/all"))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String jsonResponse = mvcResult.getResponse().getContentAsString();
        List<MediaBaseDto> responseDtos = objectMapper.readValue(jsonResponse,
                new TypeReference<>() {
                });
        assertThat(responseDtos).isEmpty();
    }

    @Test
    @DisplayName("count Media")
    void countMedia_WhenRepositoryNotEmpty_ReturnsCorrectCount() throws Exception {
        // When
        MvcResult mvcResult = mockMvc.perform(get("/media/count"))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String jsonResponse = mvcResult.getResponse().getContentAsString();
        long count = Long.parseLong(jsonResponse);
        assertThat(count).isEqualTo(EXPECTED_COUNT);
    }

    @Test
    @DisplayName("count Media when repository empty")
    void countMedia_WhenRepositoryEmpty_ReturnsZero() throws Exception {
        // When
        mediaRepository.deleteAll();
        MvcResult mvcResult = mockMvc.perform(get("/media/count"))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String jsonResponse = mvcResult.getResponse().getContentAsString();
        long count = Long.parseLong(jsonResponse);
        assertThat(count).isEqualTo(EMPTY_COUNT);
    }

    @Test
    @DisplayName("get Media by vibe")
    void getMediaByVibe_ValidCriteria_ReturnsSlice() throws Exception {
        // When
        MvcResult mvcResult = mockMvc.perform(get("/media/vibe")
                        .param(PARAM_NAME_VIBE, Vibe.MAKE_ME_FEEL_GOOD.toString())
                        .param(PARAM_NAME_YEARS, CRITERIA_YEARS_PERIOD)
                        .param(PARAM_NAME_TYPE, Type.MOVIE.toString())
                        .param(PARAM_NAME_PAGE, PAGE_NUMBER_ZERO)
                        .param(PARAM_NAME_SIZE, PAGE_SIZE_FIVE))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String jsonResponse = mvcResult.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(jsonResponse);
        JsonNode contentArray = root.get("content");

        assertThat(contentArray).isNotNull();
        assertThat(contentArray.isArray()).isTrue();
        assertThat(contentArray.size()).isGreaterThan(ZERO_OF_RECORDS);

        MediaBaseDto firstMedia = objectMapper.treeToValue(contentArray.get(FIRST_RECORD),
                MediaBaseDto.class);
        assertThat(firstMedia.getTitle()).isEqualTo(FIRST_MOVIE_TITLE);
    }

    @Test
    @DisplayName("get Media by vibe when no matches found")
    void getMediaByVibe_WhenNoMatchesFound_ReturnsEmptySlice() throws Exception {
        // When
        MvcResult mvcResult = mockMvc.perform(get("/media/vibe")
                        .param(PARAM_NAME_VIBE, NON_EXISTENT_VIBE)
                        .param(PARAM_NAME_YEARS, INVALID_YEARS_PERIOD)
                        .param(PARAM_NAME_TYPE, Type.MOVIE.toString())
                        .param(PARAM_NAME_CATEGORIES, INVALID_CATEGORIES)
                        .param(PARAM_NAME_PAGE, PAGE_NUMBER_ZERO)
                        .param(PARAM_NAME_SIZE, PAGE_SIZE_FIVE))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String jsonResponse = mvcResult.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(jsonResponse);
        JsonNode contentArray = root.get(CONTENT_STRING);

        assertThat(contentArray).isNotNull();
        assertThat(contentArray.isArray()).isTrue();
        assertThat(contentArray.size()).isEqualTo(ZERO_OF_RECORDS);
    }

    @Test
    @DisplayName("get for gallery")
    void getForGallery_ValidCriteria_ReturnsSlice() throws Exception {
        // When
        MvcResult mvcResult = mockMvc.perform(get("/media/gallery")
                        .param(PARAM_NAME_TITLE, FIRST_MOVIE_TITLE)
                        .param(PARAM_NAME_YEARS, CRITERIA_YEARS_PERIOD)
                        .param(PARAM_NAME_TYPE, Type.MOVIE.toString())
                        .param(PARAM_NAME_PAGE, PAGE_NUMBER_ZERO)
                        .param(PARAM_NAME_SIZE, PAGE_SIZE_FIVE))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String jsonResponse = mvcResult.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(jsonResponse);
        JsonNode contentArray = root.get(CONTENT_STRING);

        assertThat(contentArray).isNotNull();
        assertThat(contentArray.isArray()).isTrue();
        assertThat(contentArray.size()).isGreaterThan(ZERO_OF_RECORDS);

        MediaBaseDto galleryMedia = objectMapper.treeToValue(contentArray.get(FIRST_RECORD),
                MediaBaseDto.class);
        assertThat(galleryMedia.getTitle()).isEqualTo(FIRST_MOVIE_TITLE);
    }

    @Test
    @DisplayName("get for gallery when non existing title")
    void getForGallery_WhenNonExistingTitle_ReturnsEmptySlice() throws Exception {
        // When
        MvcResult mvcResult = mockMvc.perform(get("/media/gallery")
                        .param(PARAM_NAME_TITLE, NONEXISTENT_TITLE)
                        .param(PARAM_NAME_YEARS, INVALID_YEARS_PERIOD)
                        .param(PARAM_NAME_TYPE, Type.MOVIE.toString())
                        .param(PARAM_NAME_PAGE, PAGE_NUMBER_ZERO)
                        .param(PARAM_NAME_SIZE, PAGE_SIZE_FIVE))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String jsonResponse = mvcResult.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(jsonResponse);
        JsonNode contentArray = root.get(CONTENT_STRING);

        assertThat(contentArray).isNotNull();
        assertThat(contentArray.isArray()).isTrue();
        assertThat(contentArray.size()).isEqualTo(ZERO_OF_RECORDS);
    }

    @Test
    @DisplayName("get Luck Selection")
    void getLuckSelection_ValidSize_ReturnsSet() throws Exception {
        // When
        MvcResult mvcResult = mockMvc.perform(get("/media/luck/{size}", POPULAR_MOVIE_ID_ONE))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String jsonResponse = mvcResult.getResponse().getContentAsString();
        Set<MediaDto> responseDtos = objectMapper.readValue(jsonResponse,
                new TypeReference<>() {
                });
        assertThat(responseDtos).isNotEmpty();
    }

    @Test
    @DisplayName("get luck selection when size is zero")
    void getLuckSelection_WhenSizeIsZero_ReturnsEmptySet() throws Exception {
        // When
        MvcResult mvcResult = mockMvc.perform(get("/media/luck/{size}", EMPTY_COUNT))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String jsonResponse = mvcResult.getResponse().getContentAsString();
        Set<MediaDto> responseDtos = objectMapper.readValue(jsonResponse,
                new TypeReference<>() {
                });
        assertThat(responseDtos).isEmpty();
    }

    @Test
    @DisplayName("get recommendations")
    void getRecommendations_ValidPage_ReturnsEmptyResponse() throws Exception {
        // When
        MvcResult mvcResult = mockMvc.perform(get("/media/recommendations")
                        .param(PARAM_NAME_PAGE, PAGE_NUMBER_ZERO))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String jsonResponse = mvcResult.getResponse().getContentAsString();
        assertThat(jsonResponse).isEqualTo(EMPTY);
    }

    @Test
    @DisplayName("get Media by TopList")
    void getMediaByTopList_ValidTopList_ReturnsSlice() throws Exception {
        // When
        MvcResult mvcResult = mockMvc.perform(get("/media/top-list/{topList}",
                        TopLists.TOP_RATED_IMDB_MOVIES_OF_All_TIME.toString())
                        .param(PARAM_NAME_PAGE, PAGE_NUMBER_ZERO)
                        .param(PARAM_NAME_SIZE, PAGE_SIZE_FIVE))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        JsonNode root = objectMapper.readTree(mvcResult.getResponse().getContentAsString());
        JsonNode contentArray = root.get(CONTENT_STRING);

        assertThat(contentArray).isNotNull();
        assertThat(contentArray.isArray()).isTrue();
        assertThat(contentArray.size()).isGreaterThan(ZERO_OF_RECORDS);
    }

    @Test
    @DisplayName("get all posters")
    void getAllPosters_WhenMediaExists_ReturnsSlice() throws Exception {
        // When
        MvcResult mvcResult = mockMvc.perform(get("/media/posters")
                        .param(PARAM_NAME_PAGE, PAGE_NUMBER_ZERO)
                        .param(PARAM_NAME_SIZE, PAGE_SIZE_FIVE))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        JsonNode root = objectMapper.readTree(mvcResult.getResponse().getContentAsString());
        JsonNode contentArray = root.get(CONTENT_STRING);

        assertThat(contentArray).isNotNull();
        assertThat(contentArray.isArray()).isTrue();
        assertThat(contentArray.size()).isGreaterThan(ZERO_OF_RECORDS);
    }

    @Test
    @DisplayName("get all posters when repository empty")
    void getAllPosters_WhenRepositoryEmpty_ReturnsEmptySlice() throws Exception {
        // When
        mediaRepository.deleteAll();
        MvcResult mvcResult = mockMvc.perform(get("/media/posters")
                        .param(PARAM_NAME_PAGE, PAGE_NUMBER_ZERO)
                        .param(PARAM_NAME_SIZE, PAGE_SIZE_FIVE))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        JsonNode root = objectMapper.readTree(mvcResult.getResponse().getContentAsString());
        JsonNode contentArray = root.get(CONTENT_STRING);

        assertThat(contentArray).isNotNull();
        assertThat(contentArray.isArray()).isTrue();
        assertThat(contentArray.size()).isEqualTo(ZERO_OF_RECORDS);
    }

    @Test
    @DisplayName("get All Titles")
    void getAllTitles_WhenMediaExists_ReturnsSlice() throws Exception {
        // When
        MvcResult mvcResult = mockMvc.perform(get("/media/titles")
                        .param(PARAM_NAME_PAGE, PAGE_NUMBER_ZERO)
                        .param(PARAM_NAME_SIZE, PAGE_SIZE_FIVE))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        JsonNode root = objectMapper.readTree(mvcResult.getResponse().getContentAsString());
        JsonNode contentArray = root.get(CONTENT_STRING);

        assertThat(contentArray).isNotNull();
        assertThat(contentArray.isArray()).isTrue();
        assertThat(contentArray.size()).isGreaterThan(ZERO_OF_RECORDS);
    }

    @Test
    @DisplayName("get all titles when repository empty")
    void getAllTitles_WhenRepositoryEmpty_ReturnsEmptySlice() throws Exception {
        // When
        mediaRepository.deleteAll();
        MvcResult mvcResult = mockMvc.perform(get("/media/titles")
                        .param(PARAM_NAME_PAGE, PAGE_NUMBER_ZERO)
                        .param(PARAM_NAME_SIZE, PAGE_SIZE_FIVE))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        JsonNode root = objectMapper.readTree(mvcResult.getResponse().getContentAsString());
        JsonNode contentArray = root.get(CONTENT_STRING);

        assertThat(contentArray).isNotNull();
        assertThat(contentArray.isArray()).isTrue();
        assertThat(contentArray.size()).isEqualTo(ZERO_OF_RECORDS);
    }

    @Test
    @DisplayName("get Media by title")
    void getMediaByTitle_ExistingTitle_ReturnsMedia() throws Exception {
        // When
        MvcResult mvcResult = mockMvc.perform(get("/media/titles/{title}", FIRST_MEDIA_TITLE))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String jsonResponse = mvcResult.getResponse().getContentAsString();
        MediaDto responseDto = objectMapper.readValue(jsonResponse, MediaDto.class);
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.title()).isEqualTo(FIRST_MEDIA_TITLE);
    }

    @Test
    @DisplayName("get Media by title when Media not found")
    void getMediaByTitle_WhenMediaNotFound_ReturnsNotFound() throws Exception {
        // When
        MvcResult mvcResult = mockMvc.perform(get("/media/titles/{title}", NONEXISTENT_TITLE))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String jsonResponse = mvcResult.getResponse().getContentAsString();
        assertThat(jsonResponse).isEmpty();
    }
}
