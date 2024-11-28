package org.streaming.revenuemanagement.domain.adjustment.batch.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class VideoStatisticsUpdateDto {
    private Long videoId;
    private Long views;
    private Long adViews;
    private Long playTime;
}
