package com.auction.bid.global.security.jwt;

import com.auction.bid.global.exception.ErrorCode;
import com.auction.bid.global.exception.exceptions.AuthException;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class JWTUtil {

    private SecretKey secretKey;

    public JWTUtil(@Value("${spring.jwt.secret}") String secret) {
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm());
    }


    public String generateAccessToken(UUID memberId, String role, Long expiredMs) {
        return Jwts.builder()
                .claim("memberId", memberId.toString())
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }

    public String generateRefreshToken(UUID memberId, String role, Long expiredMs) {
        return Jwts.builder()
                .claim("memberId", memberId.toString())
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }

    public String getTokenFromHeader(String authorizationHeader) {
        return authorizationHeader.substring(7);
    }

    public String getMemberIdFromToken(String token) {
        try {
                return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("memberId", String.class);

        } catch (JwtException | IllegalArgumentException e) {
            throw new AuthException(ErrorCode.INVALID_TOKEN);
        }
    }

    public String getPassword(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("password", String.class);
    }

    public String getRole(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }

    public Boolean isTokenExpired(String token) throws JwtException, IllegalArgumentException{
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration()
                    .before(new Date());
        } catch (ExpiredJwtException e) {
            log.info("Token expired:{}", e.getMessage());
            return true;
        }
    }

    public String getMemberIdFromExpiredToken(String accessToken) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(accessToken)
                    .getPayload()
                    .get("memberId", String.class);

        } catch (ExpiredJwtException e) {
            return e.getClaims().get("memberId", String.class);
        }
    }
}
