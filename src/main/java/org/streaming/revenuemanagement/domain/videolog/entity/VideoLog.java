package org.streaming.revenuemanagement.domain.videolog.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.streaming.revenuemanagement.domain.global.entity.BaseEntity;
import org.streaming.revenuemanagement.domain.member.entity.Member;
import org.streaming.revenuemanagement.domain.video.entity.Video;
import org.streaming.revenuemanagement.domain.videolog.dto.VideoLogReqDto;

@Entity
@Table
@Getter
@ToString
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

    @Column(nullable = false)
    private Long startWatchTime = 0L;

    @Column(nullable = false)
    private Long endWatchTime = 0L;

    @Column(nullable = false)
    private Long playTime = 0L;

    @Column(nullable = false)
    private Boolean isWatchCompleted = false;


    public VideoLog(VideoLogReqDto videoLogReqDto, Video video, Member member) {

        this(videoLogReqDto);
        this.video = video;
        this.member = member;
    }

    public VideoLog(VideoLogReqDto videoLogReqDto, Video video) {

        this(videoLogReqDto);
        this.video = video;
        this.guestIp = videoLogReqDto.getGuestIp();
    }

    private VideoLog(VideoLogReqDto videoLogReqDto) {
        this.startWatchTime = videoLogReqDto.getStartWatchTime();
        this.endWatchTime = videoLogReqDto.getEndWatchTime();
        this.playTime = videoLogReqDto.getPlayTime();
        this.isWatchCompleted = videoLogReqDto.getIsWatchCompleted();
    }
}
