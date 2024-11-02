package org.revenue.management.member.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.revenue.management.common.entity.BaseEntity;

@Entity
@Table
@Getter
@NoArgsConstructor
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    private SocialType socialType;

    @Builder
    public Member(String email, String name, String username, Role role, SocialType socialType) {
        this.email = email;
        this.name = name;
        this.username = username;
        this.role = role;
        this.socialType = socialType;
    }
}
