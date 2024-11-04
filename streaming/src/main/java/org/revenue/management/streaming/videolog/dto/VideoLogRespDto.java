package org.revenue.management.streaming.videolog.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.revenue.management.streaming.videolog.entity.VideoLog;

@Getter
@Setter
@NoArgsConstructor
public class VideoLogRespDto {

    private Long id;
    private Long videoId;
    private Long memberId;
    private String guestIp;
    private Long startWatchTime;
    private Long endWatchTime;
    private Long playTime;

    public VideoLogRespDto(VideoLog videoLog) {

        this.id = videoLog.getId();
        this.videoId = videoLog.getVideo().getId();
        this.memberId = videoLog.getMember().getId();
        this.guestIp = videoLog.getGuestIp();
        this.startWatchTime = videoLog.getStartWatchTime();
        this.endWatchTime = videoLog.getEndWatchTime();
        this.playTime = videoLog.getPlayTime();
    }
}
