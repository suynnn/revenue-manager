package org.revenue.management.streaming.videoadvertisementlog.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.revenue.management.streaming.advertisement.entity.Advertisement;
import org.revenue.management.common.entity.BaseEntity;
import org.revenue.management.streaming.videolog.entity.VideoLog;

@Entity
@Table
@Getter
@NoArgsConstructor
public class VideoAdvertisementLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_log_id")
    private VideoLog videoLog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advertisement_id")
    private Advertisement advertisement;

    @Builder
    public VideoAdvertisementLog(VideoLog videoLog, Advertisement advertisement) {

        this.videoLog = videoLog;
        this.advertisement = advertisement;
    }
}
