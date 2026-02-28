package nl.prorail.tps.websocket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Valideert het JWT Bearer-token dat nginx BFF als Authorization-header meegeeft.
 * Het token wordt geverifieerd via Keycloak's JWKS-endpoint (issuer-uri in application.properties).
 *
 * Alleen requests met een geldig token bereiken de WebSocket endpoint.
 * Dit is defense-in-depth bovenop de nginx BFF session-check.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // WebSocket handshake + STOMP endpoint
                .requestMatchers("/ws/**", "/ws").authenticated()
                // Actuator health (voor Docker healthcheck)
                .requestMatchers("/actuator/health/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwt -> {})
            );

        return http.build();
    }
}
