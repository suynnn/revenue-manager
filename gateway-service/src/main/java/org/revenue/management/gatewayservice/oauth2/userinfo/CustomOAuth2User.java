package org.revenue.management.gatewayservice.oauth2.userinfo;

import org.revenue.management.member.dto.MemberDto;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {

    private final MemberDto memberDto;

    public CustomOAuth2User(MemberDto memberDto) {

        this.memberDto = memberDto;
    }

    @Override
    public Map<String, Object> getAttributes() {

        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Collection<GrantedAuthority> collection = new ArrayList<>();

        collection.add(new GrantedAuthority() {

            @Override
            public String getAuthority() {

                return memberDto.getRole().getKey();
            }
        });

        return collection;
    }

    public Long getId() {
        return memberDto.getId();
    }

    @Override
    public String getName() {

        return memberDto.getName();
    }

    public String getUsername() {

        return memberDto.getUsername();
    }
}