package org.revenue.management.member.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.revenue.management.member.entity.Role;
import org.revenue.management.member.entity.SocialType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Collections;

@Getter
@Setter
@ToString
public class MemberDto {

    private Long id;
    private String email;
    private String name;
    private String username;
    private Role role;
    private SocialType socialType;

    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 권한 정보를 SimpleGrantedAuthority로 생성하여 반환
        return Collections.singleton(new SimpleGrantedAuthority(role.getKey()));
    }
}
