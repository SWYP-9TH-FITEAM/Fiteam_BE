package com.backend.Fiteam.ConfigSecurity;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
@AllArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter{
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final ManagerDetailsService managerDetailsService;

    // 로그인, 회원가입, OAuth 콜백 등은JWT 인증 없이 접근 가능해야 한다.
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // 로그인/회원가입, 구글 로그인 콜백 등은 JWT 인증 필터 제외
        return  path.startsWith("/auth/login") ||
                path.startsWith("/auth/register") ||
                path.startsWith("/auth/google") ||
                path.startsWith("/oauth2/") ||
                path.startsWith("/auth/success") ||
                path.startsWith("/ws/chat");
    }

    // 요청마다 JWT 토큰을 꺼내서 검증하고, 인증 객체(SecurityContext)에 등록함.
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = jwtTokenProvider.resolveToken(request);

        if (token != null) {
            try {
                if (!jwtTokenProvider.validateToken(token)) {
                    Integer userId = jwtTokenProvider.getIdFromToken(token);
                    String userType = jwtTokenProvider.getTypeFromToken(token);
                    String newToken = jwtTokenProvider.createToken(userId, userType);
                    response.setHeader("Authorization", "Bearer " + newToken);
                    token = newToken;
                }

                Integer userId = jwtTokenProvider.getIdFromToken(token);
                String userType = jwtTokenProvider.getTypeFromToken(token);

                UserDetails userDetails;
                if ("manager".equals(userType)) {
                    userDetails = managerDetailsService.loadUserByUsername(String.valueOf(userId));
                } else {
                    userDetails = userDetailsService.loadUserByUsername(String.valueOf(userId));
                }

                // 4) 권한 리스트 생성 ("Manager" 또는 "User")
                List<GrantedAuthority> authorities;
                if ("manager".equals(userType)) {
                    authorities = List.of(new SimpleGrantedAuthority("ROLE_MANAGER"));
                } else if("admin".equals(userType)){
                    authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
                } else {
                    authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
                }

                // 5) Authentication 객체 생성 및 SecurityContext에 등록
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (ExpiredJwtException e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"message\": \"세션이 만료되었습니다. 다시 로그인하세요.\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

}

