package com.auction.bid.global.security.jwt;

import com.auction.bid.domain.member.dto.LoginDto;
import com.auction.bid.global.exception.ErrorCode;
import com.auction.bid.global.exception.exceptions.AuthException;
import com.auction.bid.global.security.ConstSecurity;
import com.auction.bid.global.security.RefreshToken;
import com.auction.bid.global.security.RefreshTokenRepository;
import com.auction.bid.global.security.jwt.userdetails.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JWTUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final long ACCESS_TOKEN_EXPIRATION_TIME;
    private final long REFRESH_TOKEN_EXPIRATION_TIME;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            LoginDto loginRequest = objectMapper.readValue(request.getInputStream(), LoginDto.class);
            String loginId = loginRequest.getLoginId();
            String password = loginRequest.getPassword();
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(loginId, password, null);
            return authenticationManager.authenticate(authToken);
        } catch (IOException e) {
            throw new AuthException(ErrorCode.AUTHENTICATION_FAILED);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        UUID memberId = customUserDetails.getUUID();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        Optional<RefreshToken> existingToken = refreshTokenRepository.findByMemberId(memberId);
        if (existingToken.isPresent()) {
            refreshTokenRepository.deleteByMemberId(memberId);
        }
        String accessToken = jwtUtil.generateAccessToken(memberId, role, ACCESS_TOKEN_EXPIRATION_TIME);
        String refreshToken = jwtUtil.generateRefreshToken(memberId, role, REFRESH_TOKEN_EXPIRATION_TIME);

        refreshTokenRepository.save(
                RefreshToken.builder()
                        .token(refreshToken)
                        .memberId(memberId)
                        .build()
        );

        response.addHeader(ConstSecurity.AUTHORIZATION, ConstSecurity.BEARER + accessToken);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("로그인에 성공하였습니다.");
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("로그인에 실패하였습니다.\n" +"[Error]" + failed.getMessage());
    }

}
