package com.unifor.monitoria.config;

import com.unifor.monitoria.security.JwtAuthFilter;
import com.unifor.monitoria.security.TokenAuthenticator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String JSON_UTF8 = "application/json;charset=UTF-8";

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, TokenAuthenticator tokenAuthenticator) throws Exception {
        http
            .cors(cors -> cors.disable())
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) -> {
                    res.setStatus(401);
                    res.setContentType(JSON_UTF8);
                    res.getWriter().write("{\"error\":\"Nao autenticado\"}");
                })
                .accessDeniedHandler((req, res, e) -> {
                    res.setStatus(403);
                    res.setContentType(JSON_UTF8);
                    res.getWriter().write("{\"error\":\"Acesso negado\"}");
                }))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers("/ws-chat/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/schedules/**", "/api/activities/**").hasAnyRole("MONITOR", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/schedules/**", "/api/activities/**").hasAnyRole("MONITOR", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/schedules/**", "/api/activities/**").hasAnyRole("MONITOR", "ADMIN")
                .requestMatchers("/api/presencas/**").hasAnyRole("MONITOR", "ADMIN")
                .requestMatchers("/api/**").authenticated()
                .anyRequest().denyAll()
            )
            .addFilterBefore(new JwtAuthFilter(tokenAuthenticator), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
