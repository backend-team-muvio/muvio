package org.cyberrealm.tech.muvio.config;

import static org.cyberrealm.tech.muvio.common.Constants.FIVE;
import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.brachtendorf.jimagehash.hashAlgorithms.PerceptiveHash;
import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbDiscover;
import info.movito.themoviedbapi.TmdbMovieLists;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.TmdbSearch;
import info.movito.themoviedbapi.TmdbTvSeries;
import info.movito.themoviedbapi.TmdbTvSeriesLists;
import info.movito.themoviedbapi.tools.builders.discover.DiscoverMovieParamBuilder;
import info.movito.themoviedbapi.tools.builders.discover.DiscoverTvParamBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.cyberrealm.tech.muvio.model.LocalizationEntry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
@EnableRetry
@EnableScheduling
@Configuration
public class SecurityConfig {
    private static final int PERCEPTIVE_HASH_BIT_RESOLUTION = 64;
    @Value("${tmdb.api.key}")
    private String apiKey;
    @Value("${localization.path}")
    private String localizationPath;

    @Bean
    public TmdbApi tmdbApi() {
        return new TmdbApi(apiKey);
    }

    @Bean
    public TmdbMovies tmdbMovies(TmdbApi tmdbApi) {
        return tmdbApi.getMovies();
    }

    @Bean
    public TmdbTvSeries tmdbTvSeries(TmdbApi tmdbApi) {
        return tmdbApi.getTvSeries();
    }

    @Bean
    public TmdbMovieLists tmdbMovieLists(TmdbApi tmdbApi) {
        return tmdbApi.getMovieLists();
    }

    @Bean
    public TmdbTvSeriesLists tmdbTvSeriesLists(TmdbApi tmdbApi) {
        return tmdbApi.getTvSeriesLists();
    }

    @Bean
    public TmdbSearch tmdbSearch(TmdbApi tmdbApi) {
        return tmdbApi.getSearch();
    }

    @Bean
    public TmdbDiscover tmdbDiscover(TmdbApi tmdbApi) {
        return tmdbApi.getDiscover();
    }

    @Bean
    public DiscoverMovieParamBuilder discoverMovieParamBuilder() {
        return new DiscoverMovieParamBuilder();
    }

    @Bean
    public DiscoverTvParamBuilder discoverTvParamBuilder() {
        return new DiscoverTvParamBuilder();
    }

    @Bean
    public HttpClient httpClient() {
        return HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(FIVE)).build();
    }

    @Bean
    public PerceptiveHash perceptiveHash() {
        return new PerceptiveHash(PERCEPTIVE_HASH_BIT_RESOLUTION);
    }

    @Bean
    public ObjectMapper objectMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    @Bean
    public Map<String, LocalizationEntry> localizationEntryMap(ObjectMapper objectMapper) {
        final TypeReference<Set<LocalizationEntry>> typeRef = new TypeReference<>() {};
        final Set<LocalizationEntry> localizationEntrySet;
        try (final InputStream inputStream = getClass().getClassLoader().getResourceAsStream(localizationPath)) {
            System.out.println("InputStream = " + inputStream);
            if (inputStream == null) {
                log.warn("localization.json not found in resources.");
                return Collections.emptyMap();
            }
            localizationEntrySet = objectMapper.readValue(inputStream, typeRef);
        } catch (IOException e) {
            log.error("Failed to load localization.json: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
        final Map<String, LocalizationEntry> localizationEntryMap = new HashMap<>();
        for (LocalizationEntry entry : localizationEntrySet) {
            localizationEntryMap.put(entry.getLang(), entry);
        }
        return localizationEntryMap;
    }
}
