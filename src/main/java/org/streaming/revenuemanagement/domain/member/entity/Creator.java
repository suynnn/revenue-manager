package org.streaming.revenuemanagement.domain.member.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.streaming.revenuemanagement.domain.global.entity.BaseEntity;

@Entity
@Table
@Getter
@NoArgsConstructor
public class Creator extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Builder
    public Creator(Member member) {
        this.member = member;
    }
}
