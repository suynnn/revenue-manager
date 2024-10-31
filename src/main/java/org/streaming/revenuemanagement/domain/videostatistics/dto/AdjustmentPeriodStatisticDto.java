package org.streaming.revenuemanagement.domain.videostatistics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdjustmentPeriodStatisticDto {

    private Long videoId;
    private List<AdjustmentStatisticDto> dailyStatistics;
    private List<AdjustmentStatisticDto> weeklyStatistics;
    private List<AdjustmentStatisticDto> monthlyStatistics;
}
