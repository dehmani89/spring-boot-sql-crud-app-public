package com.example.stockapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Security Configuration for Stock API
 * 
 * Implements OKTA JWT-based authentication with mixed access patterns:
 * - Public endpoints: /api/basic/**, health checks, API documentation
 * - Protected endpoints: /api/stocks/** (requires valid JWT token)
 * 
 * JWT tokens are validated against OKTA authorization server configured in application.yml
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;

    /**
     * Constructor injection for CORS configuration
     * 
     * @param corsConfigurationSource CORS configuration from CorsConfig
     */
    public SecurityConfig(CorsConfigurationSource corsConfigurationSource) {
        this.corsConfigurationSource = corsConfigurationSource;
    }

    /**
     * Configures HTTP security with JWT authentication and CORS
     * 
     * @param http HttpSecurity configuration object
     * @return SecurityFilterChain with configured security rules
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Configure URL-based authorization rules
            .authorizeHttpRequests(authz -> authz
                // Public endpoints - no authentication required
                .requestMatchers("/api/basic/**").permitAll()           // Basic controller endpoints
                .requestMatchers("/actuator/health").permitAll()        // Health check endpoint
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()  // API documentation
                
                // Protected endpoints - JWT authentication required
                .requestMatchers("/api/stocks/**").authenticated()      // Stock CRUD operations
                
                // Default: all other requests require authentication
                .anyRequest().authenticated()
            )
            // Configure OAuth2 Resource Server with JWT support
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> {})  // Use default JWT decoder with OKTA issuer from application.yml
            )
            // Enable CORS with custom configuration for React frontend
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            // Disable CSRF for stateless API (JWT tokens provide protection)
            .csrf(csrf -> csrf.disable());
        
        return http.build();
    }
}