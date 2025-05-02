package org.cyberrealm.tech.muvio.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private static final int MAX_PAGE_SIZE = 100000;
    private static final String[] ALLOWED_ORIGINS = {
            "http://localhost:5173",
            "https://furart.github.io"
    };
    private static final String[] ALLOWED_METHODS = {
            "GET", "POST", "PUT", "DELETE"
    };
    private static final String ALLOWED_PATH_PATTERN = "/**";
    private static final String ALLOWED_HEADERS = "*";
    private static final boolean ALLOW_CREDENTIALS = true;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping(ALLOWED_PATH_PATTERN)
                .allowedOrigins(ALLOWED_ORIGINS)
                .allowedMethods(ALLOWED_METHODS)
                .allowedHeaders(ALLOWED_HEADERS)
                .allowCredentials(ALLOW_CREDENTIALS);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        PageableHandlerMethodArgumentResolver resolver
                = new PageableHandlerMethodArgumentResolver();
        resolver.setMaxPageSize(MAX_PAGE_SIZE);
        resolvers.add(resolver);
    }
}
