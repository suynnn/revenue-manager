package org.streaming.revenuemanagement.domain.videostatistics.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.streaming.revenuemanagement.domain.global.entity.BaseEntity;
import org.streaming.revenuemanagement.domain.video.entity.Video;

@Entity
@Table
@Getter
@NoArgsConstructor
public class VideoStatistics extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id")
    private Video video;

    @Column(nullable = false)
    private Long totalViews = 0L;

    @Column(nullable = false)
    private Long totalAdViews = 0L;

    @Column(nullable = false)
    private Long totalPlayTime = 0L;

    @Column(nullable = false)
    private Long totalAdjustment = 0L;


    public void updateStatistics(Long totalViews, Long totalAdViews, Long totalPlayTime) {
        this.totalViews += totalViews;
        this.totalAdViews += totalAdViews;
        this.totalPlayTime += totalPlayTime;
    }

    public void updateAdjustment(Long totalAdjustment) {
        this.totalAdjustment += totalAdjustment;
    }

}
