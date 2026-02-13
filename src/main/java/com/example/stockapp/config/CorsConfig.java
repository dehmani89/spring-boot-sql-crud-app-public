package com.example.stockapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * CORS Configuration for React Frontend Integration
 * 
 * Allows React app running on localhost:3000 to call Spring Boot APIs
 * without CORS (Cross-Origin Resource Sharing) issues.
 */
@Configuration
public class CorsConfig {

    /**
     * Configure CORS settings for the application
     * 
     * @return CorsConfigurationSource with allowed origins, methods, and headers
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow React development server and production domains
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",           // React development server
            "http://localhost:3001",           // Alternative React port
            "https://your-production-domain.com"  // Replace with your production domain
        ));
        
        // Allow all HTTP methods needed for REST API
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));
        
        // Allow common headers including Authorization for JWT
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", "Content-Type", "X-Requested-With", "Accept"
        ));
        
        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // Cache preflight response for 1 hour
        configuration.setMaxAge(3600L);
        
        // Apply CORS configuration to all endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}