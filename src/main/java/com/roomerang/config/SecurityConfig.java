package com.roomerang.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(org.springframework.security.config.annotation.web.builders.HttpSecurity http) throws Exception {
        http
                // 기본 HTTP Basic 인증 사용 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)
                // CSRF 보호 비활성화 (stateless API인 경우)
                .csrf(AbstractHttpConfigurer::disable)
                // 세션을 사용하지 않음 (JWT를 사용할 경우)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // URL 별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 예시: GET 방식의 /auth/** 경로는 누구나 접근 가능
                        .requestMatchers(HttpMethod.GET, "/auth/**").permitAll()
                        // 예시: POST 방식의 /auth/** 경로는 누구나 접근 가능 (로그인, 회원가입 등)
                        .requestMatchers(HttpMethod.POST, "/auth/**").permitAll()
                        // 나머지 요청은 인증이 필요함
                        .anyRequest().authenticated()
                );

        // JWT 필터를 추가하는 경우, 여기에서 addFilterBefore(...) 등을 사용해 등록 가능
        // 예: http.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
