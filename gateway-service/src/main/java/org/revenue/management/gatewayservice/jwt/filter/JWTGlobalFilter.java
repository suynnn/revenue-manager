package org.revenue.management.gatewayservice.jwt.filter;

import lombok.RequiredArgsConstructor;
import org.revenue.management.gatewayservice.jwt.JWTUtil;
import org.revenue.management.member.dto.MemberDto;
import org.revenue.management.member.entity.Role;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JWTGlobalFilter implements WebFilter {

    private final JWTUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // 쿠키에서 Authorization을 찾음
        HttpCookie authorizationCookie = request.getCookies().getFirst("Authorization");
        if (authorizationCookie == null) {
            return chain.filter(exchange); // JWT가 없으면 다음 필터로 넘어감
        }

        String token = authorizationCookie.getValue();

        // 토큰 소멸 시간 검증
        if (jwtUtil.isExpired(token)) {
            return chain.filter(exchange); // 토큰이 만료되었으면 다음 필터로 넘어감
        }

        // 토큰에서 사용자 정보 획득
        String username = jwtUtil.getUsername(token);
        String role = jwtUtil.getRole(token);
        Long memberId = jwtUtil.getMemberId(token);

        // MemberDto 객체 생성 및 SecurityContext 설정
        MemberDto memberDto = new MemberDto();
        memberDto.setUsername(username);
        memberDto.setRole(Role.fromKey(role));
        memberDto.setId(memberId);

        // 스프링 시큐리티 인증 토큰 생성
        Authentication authentication = new UsernamePasswordAuthenticationToken(memberDto, null, memberDto.getAuthorities());

        // 리액티브 보안 컨텍스트 설정
        SecurityContext securityContext = new SecurityContextImpl(authentication);
        return chain.filter(exchange).contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)));
    }
}