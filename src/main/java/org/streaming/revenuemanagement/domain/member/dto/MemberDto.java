package org.streaming.revenuemanagement.domain.member.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.streaming.revenuemanagement.domain.member.entity.Role;
import org.streaming.revenuemanagement.domain.member.entity.SocialType;

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
