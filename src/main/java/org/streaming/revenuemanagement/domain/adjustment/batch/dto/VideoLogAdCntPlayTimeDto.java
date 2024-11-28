package org.streaming.revenuemanagement.domain.adjustment.batch.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VideoLogAdCntPlayTimeDto {
    private Integer adCnt;
    private Long playTime;
}
