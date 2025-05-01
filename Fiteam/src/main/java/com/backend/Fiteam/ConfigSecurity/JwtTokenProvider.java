package com.backend.Fiteam.ConfigSecurity;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    /*
    * 2025/04/13 - 기본적인 JWT 기능만 구현
    * 추가 - Refresh 토큰, 로그아웃 토큰 블랙리스트 처리, 토큰 만료 시간 확인 메서드
    * */

    private final Key secretKey;
    private final long tokenValidity = 1000L * 60 * 90; // 유효시간 15분
    private final long refreshThreshold = 1000L * 60 * 30; // 10분 이하 시 갱신
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    public JwtTokenProvider(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes()); // 최신 Key 생성 방식
    }

    // JWT 토큰 생성
    public String createToken(Integer id, String type) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + tokenValidity);

        return Jwts.builder()
                .setSubject(String.valueOf(id))
                .claim("type", type)  // user,manager Type도 추가로 저장
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }


    // 토큰에서 ID 추출
    public Integer getIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return Integer.valueOf(claims.getSubject());
    }

    public String getTypeFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("type", String.class);
    }


    // JWT 유효성 검증
    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Date expiration = claims.getExpiration();
            Date now = new Date();

            // 만료까지 10분 이하 남으면 갱신 필요
            return expiration.getTime() - now.getTime() >= refreshThreshold;
        } catch (ExpiredJwtException e) {
            logger.warn("JWT 만료됨: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("지원되지 않는 JWT: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("잘못된 JWT 형식: {}", e.getMessage());
        } catch (SignatureException e) {
            logger.error("JWT 서명 오류: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.warn("JWT가 비어 있음: {}", e.getMessage());
        }
        return false;
    }

    // Authorization 헤더에서 JWT 추출
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

}