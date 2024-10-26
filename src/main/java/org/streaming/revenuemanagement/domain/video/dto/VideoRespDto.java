package org.streaming.revenuemanagement.domain.video.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.streaming.revenuemanagement.domain.video.entity.Video;

@Getter
@Setter
@NoArgsConstructor
public class VideoRespDto {

    private Long id;
    private Long creatorId;
    private String videoAddr;
    private String videoTitle;
    private Integer runningTime;

    public VideoRespDto(Video video) {
        this.id = video.getId();
        this.creatorId = video.getCreator().getId();
        this.videoAddr = video.getVideoAddr();
        this.videoTitle = video.getVideoTitle();
        this.runningTime = video.getRunningTime();
    }

}
