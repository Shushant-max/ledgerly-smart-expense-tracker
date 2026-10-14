
package com.ledgerly;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Configuration
class SecurityConfig {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.cors.origins}")
    private String origins;

    SecretKey key() {
        return Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOriginPatterns(
                Arrays.stream(origins.split(","))
                        .map(String::trim)
                        .filter(origin -> !origin.isEmpty())
                        .toList()
        );

        config.setAllowedMethods(
                List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")
        );

        config.setAllowedHeaders(
                List.of("Authorization", "Content-Type")
        );

        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return source;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/health"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        new JwtFilter(key()),
                        UsernamePasswordAuthenticationFilter.class
                )
                .exceptionHandling(exceptions ->
                        exceptions.authenticationEntryPoint(
                                (request, response, exception) ->
                                        response.sendError(
                                                HttpServletResponse.SC_UNAUTHORIZED,
                                                "Authentication required"
                                        )
                        )
                )
                .build();
    }

    static class JwtFilter extends OncePerRequestFilter {

        private final SecretKey key;

        JwtFilter(SecretKey key) {
            this.key = key;
        }

        @Override
        protected boolean shouldNotFilter(
                HttpServletRequest request
        ) {
            String path = request.getServletPath();

            return "OPTIONS".equalsIgnoreCase(request.getMethod())
                    || path.startsWith("/api/auth/")
                    || path.equals("/api/health");
        }

        @Override
        protected void doFilterInternal(
                HttpServletRequest request,
                HttpServletResponse response,
                FilterChain chain
        ) throws ServletException, IOException {

            String header = request.getHeader("Authorization");

            if (header != null && header.startsWith("Bearer ")) {
                try {
                    Claims claims = Jwts.parser()
                            .verifyWith(key)
                            .build()
                            .parseSignedClaims(header.substring(7))
                            .getPayload();

                    Long id = Long.valueOf(claims.getSubject());

                    var authentication =
                            new UsernamePasswordAuthenticationToken(
                                    id.toString(),
                                    null,
                                    List.of()
                            );

                    SecurityContextHolder.getContext()
                            .setAuthentication(authentication);

                } catch (Exception exception) {
                    SecurityContextHolder.clearContext();

                    response.sendError(
                            HttpServletResponse.SC_UNAUTHORIZED,
                            "Invalid or expired token"
                    );
                    return;
                }
            }

            chain.doFilter(request, response);
        }
    }
}

            }

            chain.doFilter(request, response);
        }
    }
}
