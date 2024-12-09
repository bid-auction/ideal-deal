package com.auction.bid.global.security.jwt;

import com.auction.bid.domain.member.Member;
import com.auction.bid.global.exception.ErrorCode;
import com.auction.bid.global.exception.exceptions.MemberException;
import com.auction.bid.global.security.ConstSecurity;
import com.auction.bid.global.security.RefreshToken;
import com.auction.bid.global.security.RefreshTokenRepository;
import com.auction.bid.global.security.jwt.userdetails.CustomUserDetails;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static com.auction.bid.global.security.ConstSecurity.*;

@Slf4j
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RefreshTokenRepository refreshTokenRepository;
    private final long ACCESS_TOKEN_EXPIRATION_TIME;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader(AUTHORIZATION);

        if (authorization == null || !authorization.startsWith(BEARER)) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = authorization.substring(BEARER.length()).trim();

        if (isLogout(accessToken)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("로그아웃된 사용자입니다.");
            return;
        }

        try {
            if (jwtUtil.isTokenExpired(accessToken)) {

                String memberId = jwtUtil.getMemberIdFromExpiredToken(accessToken);
                UUID memberUUID = UUID.fromString(memberId);

                try {
                    if (isRefreshTokenExpired(memberUUID)) {
                        refreshTokenRepository.deleteByMemberId(memberUUID);
                        filterChain.doFilter(request, response);
                        return;
                    }
                } catch (MemberException e) {
                    log.info("Member exception={}", e.getMessage());
                    filterChain.doFilter(request, response);
                    return;
                }

                String newAccessToken = jwtUtil.generateAccessToken(
                        memberUUID,
                        ROLE_MEMBER,
                        ACCESS_TOKEN_EXPIRATION_TIME
                );

                response.setHeader(ConstSecurity.AUTHORIZATION, ConstSecurity.BEARER + newAccessToken);
                accessToken = newAccessToken;
            }
        } catch (JwtException | IllegalArgumentException e) {
            log.error("JwtException={}", e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }


        String findMemberId = jwtUtil.getMemberIdFromToken(accessToken);
        String findPassword = jwtUtil.getPassword(accessToken);
        String findRole = jwtUtil.getRole(accessToken);

        Member findMember = Member.builder()
                .memberId(UUID.fromString(findMemberId))
                .password(findPassword)
                .role(findRole)
                .build();

        CustomUserDetails customUserDetails = new CustomUserDetails(findMember);

        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);
        filterChain.doFilter(request, response);
    }

    private boolean isLogout(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(token));
    }

    private boolean isRefreshTokenExpired(UUID memberId) {
        RefreshToken refreshToken = refreshTokenRepository.findByMemberId(memberId)
                .orElseThrow(() -> new MemberException(ErrorCode.TOKEN_NOT_FOUND));

        return jwtUtil.isTokenExpired(refreshToken.getToken());
    }

}
