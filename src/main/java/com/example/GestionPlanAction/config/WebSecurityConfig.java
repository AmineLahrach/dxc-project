package com.example.GestionPlanAction.config;

import com.example.GestionPlanAction.security.AuthEntryPointJwt;
import com.example.GestionPlanAction.security.AuthTokenFilter;
import com.example.GestionPlanAction.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> 
                auth
                    // Public endpoints - no authentication required
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/test/**").permitAll()
                    .requestMatchers("/swagger-ui.html").permitAll()
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                    
                    // Temporarily allow users endpoint for testing
                    .requestMatchers("/api/users/**").permitAll()
                    // Debug endpoint for checking authentication
                    .requestMatchers("/api/test/auth-debug").permitAll()
                    // Temporarily allow servicelines for testing
                    .requestMatchers("/api/servicelines/**").permitAll()
                    
                    // Admin only endpoints
                    .requestMatchers("/api/profils/**").hasRole("ADMINISTRATEUR")
                    .requestMatchers("/api/audit/**").hasRole("ADMINISTRATEUR")
                    
                    // Director endpoints
                    .requestMatchers("/api/exercices/**").hasAnyRole("ADMINISTRATEUR", "DIRECTEUR_GENERAL")
                    .requestMatchers("/api/plans/*/approve").hasAnyRole("ADMINISTRATEUR", "DIRECTEUR_GENERAL")
                    .requestMatchers("/api/analytics/**").hasAnyRole("ADMINISTRATEUR", "DIRECTEUR_GENERAL")
                    
                    // Collaborator and above endpoints
                    .requestMatchers("/api/plans/**").hasAnyRole("ADMINISTRATEUR", "DIRECTEUR_GENERAL", "COLLABORATEUR")
                    .requestMatchers("/api/variable-actions/**").hasAnyRole("ADMINISTRATEUR", "DIRECTEUR_GENERAL", "COLLABORATEUR")
                    .requestMatchers("/api/notifications/**").authenticated()
                    
                    // All other requests require authentication
                    .anyRequest().authenticated()
            );

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        // For H2 Console (remove in production)
        http.headers(headers -> headers.frameOptions().sameOrigin());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}