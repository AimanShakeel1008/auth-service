package com.aiplms.auth.config;

import com.aiplms.auth.repository.UserRepository;
import com.aiplms.auth.security.JwtAuthenticationEntryPoint;
import com.aiplms.auth.security.JwtAuthenticationFilter;
import com.aiplms.auth.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtService, userRepository);
    }

    @Bean
    public JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint() {
        return new JwtAuthenticationEntryPoint(objectMapper);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // Open auth endpoints
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // JWKS & actuator and health
                        .requestMatchers("/.well-known/jwks.json", "/actuator/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // Static resources & templates
                        .requestMatchers("/", "/index.html", "/static/**", "/favicon.ico").permitAll()
                        // everything else under /api/v1/me must be authenticated - example
                        .requestMatchers("/api/v1/me", "/api/v1/me/**").authenticated()
                        // default: authenticated
                        .anyRequest().permitAll()
                )

                .cors(Customizer.withDefaults())

                .exceptionHandling(e -> e.authenticationEntryPoint(jwtAuthenticationEntryPoint()))

                // No httpBasic / form login
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable);

        // Add JWT filter before UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}


