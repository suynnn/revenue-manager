package org.streaming.revenuemanagement.domain.videolog.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class VideoLogReqDto {

    private Long videoId;
    private Long creatorId;
    private Long memberId;
    private String guestIp;
    private Long startWatchTime;
    private Long endWatchTime;
    private Long playTime;
    private Boolean isWatchCompleted;
    private Long uuid;
}
