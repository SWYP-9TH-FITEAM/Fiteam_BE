package com.backend.Fiteam.ConfigSecurity;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter{
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    public JwtAuthFilter(JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    // 로그인, 회원가입, OAuth 콜백 등은JWT 인증 없이 접근 가능해야 한다.
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // 로그인/회원가입, 구글 로그인 콜백 등은 JWT 인증 필터 제외
        return  path.startsWith("/auth/login") ||
                path.startsWith("/auth/register") ||
                path.startsWith("/auth/google") ||
                path.startsWith("/oauth2/") ||
                path.startsWith("/auth/success");
    }

    // 요청마다 JWT 토큰을 꺼내서 검증하고, 인증 객체(SecurityContext)에 등록함.
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Request 헤더에서 토큰 추출
        String token = jwtTokenProvider.resolveToken(request);

        if (token != null) {
            try {
                if (!jwtTokenProvider.validateToken(token)) { // 만료 시간이 20분 이하일 경우 새로운 토큰 발급
                    Integer userId = jwtTokenProvider.getIdFromToken(token);
                    String newToken = jwtTokenProvider.createToken(userId);
                    response.setHeader("Authorization", "Bearer " + newToken);
                    token = newToken;
                }

                Integer userId = jwtTokenProvider.getIdFromToken(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(String.valueOf(userId));

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (ExpiredJwtException e) {
                // 토큰이 아예 만료된 경우 → 401 응답 반환
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"message\": \"세션이 만료되었습니다. 다시 로그인하세요.\"}");
                return;
            }
        }

        // 다음 필터(혹은 컨트롤러)로 요청 전달
        filterChain.doFilter(request, response);
    }

}

