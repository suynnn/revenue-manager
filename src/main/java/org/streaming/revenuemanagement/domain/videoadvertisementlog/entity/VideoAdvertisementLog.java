package org.streaming.revenuemanagement.domain.videoadvertisementlog.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.streaming.revenuemanagement.domain.advertisement.entity.Advertisement;
import org.streaming.revenuemanagement.domain.global.entity.BaseEntity;
import org.streaming.revenuemanagement.domain.videolog.entity.VideoLog;

@Entity
@Table
@Getter
@NoArgsConstructor
public class VideoAdvertisementLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "video_log_id")
    private VideoLog videoLog;

    @ManyToOne
    @JoinColumn(name = "advertisement_id")
    private Advertisement advertisement;
}
