package com.auction.bid.global.security.jwt;

import com.auction.bid.domain.member.Member;
import com.auction.bid.global.security.userdetails.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.auction.bid.global.security.ConstSecurity.*;

@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader(AUTHORIZATION);

        if (authorization == null || !authorization.startsWith(BEARER)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.split(" ")[1];

        if (jwtUtil.isExpired(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (isLogout(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("로그아웃된 사용자입니다.");
            return;
        }

        String findEmail = jwtUtil.getEmail(token);
        String findPassword = jwtUtil.getPassword(token);
        String findRole = jwtUtil.getRole(token);

        Member findMember = Member.builder()
                .email(findEmail)
                .password(findPassword)
                .role(findRole)
                .build();

        CustomUserDetails customUserDetails = new CustomUserDetails(findMember);

        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);
        filterChain.doFilter(request, response);
    }

    private boolean isLogout(String token) {
        boolean equals = Boolean.TRUE.equals(redisTemplate.hasKey(token));
        return equals;
    }
}
