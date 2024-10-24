package org.streaming.revenuemanagement.domain.video.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.streaming.revenuemanagement.domain.global.entity.BaseEntity;
import org.streaming.revenuemanagement.domain.member.entity.Creator;

@Entity
@Table
@Getter
@NoArgsConstructor
public class Video extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private Creator creator;

    @Column(nullable = false)
    private String videoAddr;

    @Column(nullable = false)
    private String videoTitle;

    @Column(nullable = false)
    private Integer runningTime;

    @Column(nullable = false)
    private Boolean isPrivate;

    @Column(nullable = false)
    private Boolean isDeleted = false;
}
