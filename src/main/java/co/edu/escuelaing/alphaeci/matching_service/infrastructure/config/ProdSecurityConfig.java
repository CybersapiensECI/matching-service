package co.edu.escuelaing.alphaeci.matching_service.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@Profile("!dev")
public class ProdSecurityConfig {

    @SuppressWarnings("java:S4502")
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, KongAuthFilter kongAuthFilter) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .addFilterBefore(kongAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/actuator/health",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**"
                ).permitAll()
                // Read-only category/tag catalog: static reference data (no PII), needed both
                // by the client's interest picker and by profile-service's server-to-server
                // call in MatchingServiceAdapter, which sends no credential. Same treatment as
                // GeoService's public zone catalog. Writes stay behind auth.
                .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()
                .anyRequest().authenticated()
            );

        return http.build();
    }
}
