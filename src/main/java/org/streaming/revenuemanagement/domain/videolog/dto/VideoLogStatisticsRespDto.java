package org.streaming.revenuemanagement.domain.videolog.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VideoLogStatisticsRespDto {

    private Long videoId;
    private Long views; // 조회수 (VideoLog 개수)
    private Long adViews; // 광고 조회수 합
    private Long playTime; // 재생 시간 합
}
