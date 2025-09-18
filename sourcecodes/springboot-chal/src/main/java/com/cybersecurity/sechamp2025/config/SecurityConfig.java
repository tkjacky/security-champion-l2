package com.cybersecurity.sechamp2025.config;

import com.cybersecurity.sechamp2025.utils.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))
            .authorizeHttpRequests(auth -> auth
                // API endpoints first (more specific)
                .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/demo/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/books/**").permitAll()
                .requestMatchers("/api/users").authenticated()
                .requestMatchers("/api/profile/**").authenticated()
                .requestMatchers("/api/user/**").authenticated()
                .requestMatchers("/api/admin/**").authenticated()
                .requestMatchers("/api/cart/**").authenticated()
                .requestMatchers("/api/purchase/**").authenticated()
                // Static resources
                .requestMatchers("/css/**", "/js/**", "/favicon.ico", "/favicon.svg").permitAll()
                .requestMatchers("/bak/**").permitAll()
                // Page routes
                .requestMatchers("/", "/login", "/register", "/books", "/bookstore-location", "/help", "/advanced_search", "/advanced_search/**").permitAll()
                .requestMatchers("/users", "/profile", "/admin").permitAll()
                .requestMatchers("/purchase/**").permitAll()
                .requestMatchers("/cart", "/checkout").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable());

        return http.build();
    }
}
