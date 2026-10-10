package com.hercufy.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    @Autowired
    private RateLimitingFilter rateLimitingFilter;

    @Bean
    SecurityFilterChain defaultFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter,
                                          RateLimitingFilter rateLimitingFilter,
                                          CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests((request) -> request
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${application.cors.allowed-origins:}") String[] allowedOrigins) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(Stream.concat(
                // Local: el ng serve de desarrollo, la app servida como PWA y el contenedor de
                // Capacitor. Red privada: es lo que hace posible abrir la app en el movil por
                // http://192.168.x.x:4200 sin tener que declarar cada IP. En produccion se acota
                // con CORS_ALLOWED_ORIGINS (esta lista de abajo solo son valores por defecto).
                Stream.of("http://localhost", "http://localhost:*", "https://localhost",
                        "capacitor://localhost", "http://127.0.0.1:*", "http://192.168.*:*", "http://10.*:*",
                        "http://172.16.*:*", "http://172.17.*:*", "http://172.18.*:*",
                        "http://172.19.*:*", "http://172.20.*:*", "http://172.21.*:*",
                        "http://172.22.*:*", "http://172.23.*:*", "http://172.24.*:*",
                        "http://172.25.*:*", "http://172.26.*:*", "http://172.27.*:*",
                        "http://172.28.*:*", "http://172.29.*:*", "http://172.30.*:*",
                        "http://172.31.*:*"),
                Arrays.stream(allowedOrigins)
                        .map(String::trim)
                        .filter(origin -> !origin.isEmpty())
                        .flatMap(origin -> Stream.of(origin.split(",")))
                        .map(String::trim)
                        .filter(origin -> !origin.isEmpty())).distinct().toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Origin"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

}
