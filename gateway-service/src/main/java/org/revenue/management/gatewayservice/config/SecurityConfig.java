package org.revenue.management.gatewayservice.config;

import lombok.RequiredArgsConstructor;
import org.revenue.management.gatewayservice.jwt.JWTUtil;
import org.revenue.management.gatewayservice.jwt.filter.JWTGlobalFilter;
import org.revenue.management.gatewayservice.oauth2.handler.CustomSuccessHandler;
import org.revenue.management.gatewayservice.oauth2.service.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomSuccessHandler customSuccessHandler;
    private final JWTUtil jwtUtil;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                // CSRF 비활성화
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                // 로그인 방식 비활성화
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)

                // JWT 필터 추가
                .addFilterBefore(new JWTGlobalFilter(jwtUtil), SecurityWebFiltersOrder.AUTHENTICATION)

                // OAuth2 설정
                .oauth2Login(oauth2 -> oauth2
                        .authenticationSuccessHandler((webFilterExchange, authentication) -> {
                            customSuccessHandler.onAuthenticationSuccess(
                                    webFilterExchange.getExchange().getRequest(),
                                    webFilterExchange.getExchange().getResponse(),
                                    authentication);
                            return webFilterExchange.getChain().filter(webFilterExchange.getExchange());
                        })
                )

                // 경로별 인가 작업
                .authorizeExchange(auth -> auth
                        .pathMatchers("/").permitAll()
                        .anyExchange().authenticated()
                )

                // 세션 사용 안 함 (JWT를 사용하므로 세션을 사용할 필요 없음)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())

                .build();
    }
}