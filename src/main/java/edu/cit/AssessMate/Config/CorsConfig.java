package edu.cit.AssessMate.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);

        // ** THIS MUST MATCH YOUR FRONTEND'S EXACT ORIGIN **
        //config.addAllowedOrigin("http://localhost:5173");
        config.addAllowedOrigin("https://assessmatefinal-6cog.vercel.app");
        // Add other origins if needed, e.g., config.addAllowedOrigin("http://localhost:3000");

        config.addAllowedHeader("*"); // Allow all headers
        config.addExposedHeader("Authorization"); // Expose Authorization header
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("OPTIONS"); // Crucial for preflight requests

        config.setMaxAge(3600L);

        source.registerCorsConfiguration("/**", config); // Apply to all paths

        return new CorsFilter(source);
    }
}