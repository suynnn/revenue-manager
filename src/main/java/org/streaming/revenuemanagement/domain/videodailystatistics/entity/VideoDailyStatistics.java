package org.streaming.revenuemanagement.domain.videodailystatistics.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.streaming.revenuemanagement.domain.global.entity.BaseEntity;
import org.streaming.revenuemanagement.domain.videostatistics.entity.VideoStatistics;

@Entity
@Table
@Getter
@NoArgsConstructor
public class VideoDailyStatistics extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_statistics_id")
    private VideoStatistics videoStatistics;

    @Column(nullable = false)
    private Long videoId;

    @Column(nullable = false)
    private Long dailyViews = 0L;

    @Column(nullable = false)
    private Long dailyAdViews = 0L;

    @Column(nullable = false)
    private Long dailyPlayTime = 0L;

    @Column(nullable = false)
    private Long dailyAdjustment = 0L;

    @Column(nullable = false)
    private Long dailyAdAdjustment = 0L;

    @Builder
    public VideoDailyStatistics(VideoStatistics videoStatistics, Long videoId) {

        this.videoStatistics = videoStatistics;
        this.videoId = videoId;
    }

    public void updateStatistics(Long views, Long adViews, Long playTime) {

        this.dailyViews += views;
        this.dailyAdViews += adViews;
        this.dailyPlayTime += playTime;
    }

    public void updateAdjustment(Long adjustment) {

        this.dailyAdjustment += adjustment;
    }

    public void updateAdAdjustment(Long adAdjustment) {

        this.dailyAdAdjustment += adAdjustment;
    }
}
