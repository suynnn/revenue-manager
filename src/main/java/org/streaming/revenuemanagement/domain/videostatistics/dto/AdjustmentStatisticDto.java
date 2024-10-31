package org.streaming.revenuemanagement.domain.videostatistics.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdjustmentStatisticDto {
    private Long videoId;
    private Long totalAdjustment;
    private Long totalAdAdjustment;
    private Long totalCombinedAdjustment;

    public AdjustmentStatisticDto(Long videoId, Long totalAdjustment, Long totalAdAdjustment) {
        this.videoId = videoId;
        this.totalAdjustment = totalAdjustment;
        this.totalAdAdjustment = totalAdAdjustment;
        this.totalCombinedAdjustment = totalAdjustment + totalAdAdjustment;
    }
}
