package org.cyberrealm.tech.muvio.config;

import com.uwetrottmann.tmdb2.Tmdb;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityConfig {
    @Value("${tmdb.api.key}")
    private String apiKey;

    @Bean
    public Tmdb tmdb() {
        return new Tmdb(apiKey);
    }
}
