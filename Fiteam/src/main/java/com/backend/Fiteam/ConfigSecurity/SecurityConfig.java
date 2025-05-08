package com.backend.Fiteam.ConfigSecurity;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private static final List<String> ALLOWED_ORIGINS = List.of(
            "http://localhost:3000",
            "http://localhost:5173",
            "http://localhost:8080",
            "https://swyp-9th-fiteam.github.io",

            "https://fiteam.shop",
            "https://www.fiteam.shop"
            // https 프론트나 다른 서버 작성
    );

    // API endponit 허용할거 작성.
    public static final String[] PERMIT_URLS = {
            // 기본/Swagger
            "/error",
            "/favicon.ico",
            "/v3/api-docs",
            "/swagger-ui/index.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/swagger-ui.html",
            "/webjars/**",

            // Auth
            "/v1/auth/test",
            "/v1/auth/login",
            "/v1/auth/register",
            "/v1/auth/verify-email",
            "/v1/auth/reset-password",

            // Character (인증 없이)
            "/v1/card/*",
            "/v1/question/all",
            "/v1/question/unauth/test-result",

            // Chat
            "/v1/chat/room",
            "/v1/chat/list",
            "/v1/chat/*/messages",
            // WebSocket STOMP
            "/ws/chat/**",

            // ProjectGroup
            "/v1/group/create",
            "/v1/group/set-teamtype/*",
            "/v1/group/invite",
            "/v1/group/*/members",

            // GroupMemberController 관련 허용 API 경로
            "/v1/member/{groupId}/positions",
            "/v1/member/profile/{groupMemberId}",
            "/v1/member/myprofile/mini",
            "/v1/member/profile/my",
            "/v1/member/profile/{memberId}",
            "/v1/member/{groupId}/members",
            "/v1/member/my",

            // Notification
            "/v1/noti/notifications",

            // Team
            "/v1/team/request",
            "/v1/team/requests/received",
            "/v1/team/request/from/*",
            "/v1/team/request/accept/*",
            "/v1/team/request/reject/*",
            "/v1/team/myteam",
            "/v1/team/teambuildingstatus",

            // User
            "/v1/user/savecard",
            "/v1/user/mini-result",
            "/v1/user/card",
            "/v1/user/name-img-job",
            "/v1/user/accept/**",
            "/v1/user/groups/accepted",
            "/v1/user/groups/pending",
            "/v1/user/settings",
            "/v1/user/settings",

            // UserLike
            "/v1/like/add",
            "/v1/like/unlike/*",
            "/v1/like/likelist",
            "/v1/like/memo/*",
    };


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /*
    * 2025/04/13 - CORS, JWT, form login 비활성화
    * 1. 로그인은 했지만 권한 부족(예: 403 Forbidden)인 경우 응답을 커스터마이징
    * 2. OAuth2 로그인 연동(FE 와 상의필요)
    * */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .formLogin(form -> form.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PERMIT_URLS).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((_, response, _) -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\": \"Unauthorized access\"}");
                        })
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(ALLOWED_ORIGINS);
        config.setAllowedMethods(List.of("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        source.registerCorsConfiguration("/**", config);
        return request -> {
            String origin = request.getHeader("Origin");
            if (origin != null && !ALLOWED_ORIGINS.contains(origin)) {
                // System.out.println("CORS 차단: " + origin + "는 허용되지 않은 Origin입니다!");
                throw new RuntimeException("CORS 차단: " + origin + "는 허용되지 않은 Origin입니다!");
            }
            return config;
        };
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
