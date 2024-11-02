package org.revenue.management.member.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.revenue.management.member.entity.Role;
import org.revenue.management.member.entity.SocialType;

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
}
