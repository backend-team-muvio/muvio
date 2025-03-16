package org.cyberrealm.tech.muvio.config;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

import info.movito.themoviedbapi.TmdbApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.retry.annotation.EnableRetry;

@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
@EnableRetry
@Configuration
public class SecurityConfig {
    @Value("${tmdb.api.key}")
    private String apiKey;

    @Bean
    public TmdbApi tmdbApi() {
        return new TmdbApi(apiKey);
    }
}
