package org.revenue.management.streaming.streaming.util;

import jakarta.servlet.http.HttpServletRequest;
import org.revenue.management.member.oauth2.userinfo.CustomOAuth2User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class UserUtils {

    public String getUserId(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof CustomOAuth2User) {
            // CustomOAuth2User로 캐스팅하여 사용자 정보 가져오기
            CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
            return customOAuth2User.getUsername(); // 로그인된 사용자의 ID
        } else {
            // 익명 사용자 (IP 주소로 구분)
            return "ip " + request.getRemoteAddr(); // 익명 사용자의 IP 주소
        }
    }
}
