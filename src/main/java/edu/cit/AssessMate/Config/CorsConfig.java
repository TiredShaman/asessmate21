package edu.cit.AssessMate.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
<<<<<<< HEAD
=======
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

>>>>>>> 075e00d5e434c55adcfda3a2d7eeaec99b6c3f11
import java.util.Arrays;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                    "https://assessmatefinal-6cog.vercel.app",
                    "http://localhost:3000",
                    "https://assessmate-j21k.onrender.com"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH")
                .allowedHeaders("*")
                .exposedHeaders("Authorization")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Bean 
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        config.setAllowedOrigins(Arrays.asList(
            "https://assessmatefinal-6cog.vercel.app",
            "http://localhost:3000", 
            "https://assessmate-j21k.onrender.com"
        ));
        config.setAllowCredentials(true);
<<<<<<< HEAD

        // Explicitly list all allowed origins
        config.setAllowedOrigins(Arrays.asList(
            "http://localhost:5173",
            "http://localhost:3000",
            "https://assessmatefinal-6cog.vercel.app",
            "https://assessmate-j21k.onrender.com"
        ));

        config.addAllowedHeader("*");
        config.addExposedHeader("Authorization");
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setMaxAge(3600L);

        source.registerCorsConfiguration("/**", config);

=======
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setExposedHeaders(Arrays.asList("Authorization"));
        config.setMaxAge(3600L);
        
        source.registerCorsConfiguration("/**", config);
>>>>>>> 075e00d5e434c55adcfda3a2d7eeaec99b6c3f11
        return new CorsFilter(source);
    }
}