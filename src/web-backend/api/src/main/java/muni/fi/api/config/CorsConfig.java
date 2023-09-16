package muni.fi.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {

        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@Nullable CorsRegistry registry) {
                if (registry != null) {
                    registry
                            .addMapping("/**")
                            .allowedMethods(CorsConfiguration.ALL)
                            .allowedHeaders(CorsConfiguration.ALL)
                            .allowedOriginPatterns(CorsConfiguration.ALL);
                }
            }
        };
    }
}
