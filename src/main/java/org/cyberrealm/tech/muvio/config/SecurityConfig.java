package org.cyberrealm.tech.muvio.config;

import com.uwetrottmann.tmdb2.Tmdb;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class SecurityConfig {
    @Value("${tmdb.api.key}")
    private String apiKey;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public Tmdb tmdb(RestTemplate restTemplate) {
        return new Tmdb(apiKey);
    }
}
