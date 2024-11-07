package org.revenue.management.gatewayservice.oauth2.handler;

import org.revenue.management.gatewayservice.jwt.JWTUtil;
import org.revenue.management.gatewayservice.oauth2.userinfo.CustomOAuth2User;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Iterator;

@Component
public class CustomSuccessHandler {

    private final JWTUtil jwtUtil;

    public CustomSuccessHandler(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public Mono<Void> onAuthenticationSuccess(ServerHttpRequest request, ServerHttpResponse response, Authentication authentication) {

        // OAuth2User
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();

        String username = customUserDetails.getUsername();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        Long memberId = customUserDetails.getId();

        String token = jwtUtil.createJwt(username, role, memberId, 60 * 60 * 60L * 1000);

        // 기존 JWT가 만료된 경우 쿠키 삭제
        deleteCookie(response, "Authorization");

        // 새로운 JWT를 쿠키로 설정
        response.addCookie(createCookie("Authorization", token));

        // 리다이렉트 처리
        response.setStatusCode(org.springframework.http.HttpStatus.FOUND);
        response.getHeaders().setLocation(java.net.URI.create("http://localhost:8080/"));

        return response.setComplete();
    }

    private ResponseCookie createCookie(String key, String value) {
        return ResponseCookie.from(key, value)
                .maxAge(60 * 60 * 60)
                .path("/")
                .httpOnly(true)
                .build();
    }

    private void deleteCookie(ServerHttpResponse response, String key) {
        ResponseCookie cookie = ResponseCookie.from(key, null)
                .maxAge(0)
                .path("/")
                .build();
        response.addCookie(cookie);
    }
}

