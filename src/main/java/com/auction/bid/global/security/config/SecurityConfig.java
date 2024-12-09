package com.auction.bid.global.security.config;

<<<<<<< HEAD
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
=======
import com.auction.bid.global.security.ConstSecurity;
import com.auction.bid.global.security.RefreshTokenRepository;
import com.auction.bid.global.security.jwt.JWTFilter;
import com.auction.bid.global.security.jwt.JWTUtil;
import com.auction.bid.global.security.jwt.LoginFilter;
import com.auction.bid.global.security.oauth2.CustomAuthenticationFailureHandler;
import com.auction.bid.global.security.oauth2.CustomAuthenticationSuccessHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
>>>>>>> develop
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

<<<<<<< HEAD
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 보호 비활성화 (Postman 테스트 용도)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/product").permitAll() // 인증 없이 접근 가능// SELLER 권한 필요
                        .anyRequest().authenticated() // 그 외 요청은 인증 필요
                )
                .formLogin(form -> form.disable()) // 기본 폼 로그인 비활성화
                .httpBasic(httpBasic -> httpBasic.disable()); // JWT 인증 사용 설정
=======
    private final JWTUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    @Value("${spring.jwt.access-token.expiration-time}")
    private long ACCESS_TOKEN_EXPIRATION_TIME;

    @Value("${spring.jwt.refresh-token.expiration-time}")
    private long REFRESH_TOKEN_EXPIRATION_TIME;

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        LoginFilter loginFilter = new LoginFilter(
                authenticationManager(authenticationConfiguration),
                refreshTokenRepository,
                jwtUtil,
                objectMapper,
                ACCESS_TOKEN_EXPIRATION_TIME,
                REFRESH_TOKEN_EXPIRATION_TIME
        );
        loginFilter.setFilterProcessesUrl("/api/member/auth/loginFilter");

        http
                .csrf(AbstractHttpConfigurer::disable);

        http
                .formLogin(AbstractHttpConfigurer::disable);

        http
                .httpBasic(AbstractHttpConfigurer::disable);

        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers(HttpMethod.POST,
                                "/api/member/auth/signup",
                                "/api/member/auth/send-email",
                                "/api/member/auth/verify-email").permitAll()
                        .requestMatchers("/login").permitAll()
//                        .requestMatchers(HttpMethod.GET,
//                                "/api/member/test").permitAll()
                        .anyRequest().authenticated());

        http
                .oauth2Login(oauth ->
                        oauth
                                .successHandler(customAuthenticationSuccessHandler)
                                .failureHandler(customAuthenticationFailureHandler)
                );

        http
                .addFilterBefore(new JWTFilter(
                        jwtUtil,
                        redisTemplate,
                        refreshTokenRepository,
                        ACCESS_TOKEN_EXPIRATION_TIME
                ), LoginFilter.class);

        http
                .addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class);

        http
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
>>>>>>> develop



        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // 비밀번호 인코딩 설정
    }
}