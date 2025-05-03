package edu.cit.AssessMate.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                    "https://assessmatefinal-6cog.vercel.app",
                    "http://localhost:3000", 
                    "https://assessmate-j21k.onrender.com",
                    "https://assessmatefinal-6cog.vercel.app/"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH")
                .allowedHeaders("*")
                .exposedHeaders(
                    "Authorization", 
                    "Access-Control-Allow-Origin",
                    "Access-Control-Allow-Credentials",
                    "Access-Control-Allow-Headers",
                    "Access-Control-Allow-Methods",
                    "Access-Control-Max-Age"
                )
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Allow specified origins
        config.addAllowedOrigin("https://assessmatefinal-6cog.vercel.app");
        config.addAllowedOrigin("http://localhost:3000");
        config.addAllowedOrigin("https://assessmate-j21k.onrender.com"); 
        config.addAllowedOrigin("https://assessmatefinal-6cog.vercel.app/");
        
        // Allow credentials and cookies
        config.setAllowCredentials(true);
        
        // Allow all headers and methods
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        
        // Expose all necessary headers
        config.addExposedHeader("Authorization");
        config.addExposedHeader("Access-Control-Allow-Origin");
        config.addExposedHeader("Access-Control-Allow-Credentials");
        config.addExposedHeader("Access-Control-Allow-Headers");
        config.addExposedHeader("Access-Control-Allow-Methods");
        config.addExposedHeader("Access-Control-Max-Age");
        
        // Set longer max age for preflight caching
        config.setMaxAge(7200L);

        source.registerCorsConfiguration("/api/students/**", config);
        source.registerCorsConfiguration("/api/auth/**", config);
        return new CorsFilter(source);
    }
}