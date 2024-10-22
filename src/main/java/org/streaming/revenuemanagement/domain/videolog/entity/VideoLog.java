package org.streaming.revenuemanagement.domain.videolog.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.streaming.revenuemanagement.domain.global.entity.BaseEntity;
import org.streaming.revenuemanagement.domain.member.entity.Member;
import org.streaming.revenuemanagement.domain.video.entity.Video;

@Entity
@Table
@Getter
@NoArgsConstructor
public class VideoLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    private String guestIp;

    @JoinColumn(nullable = false)
    private Long startWatchTime = 0L;

    @JoinColumn(nullable = false)
    private Long endWatchTime = 0L;

    @JoinColumn(nullable = false)
    private Long playTime = 0L;
}
