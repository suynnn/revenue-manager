package org.revenue.management.batch.videodailystatistics.dto;

import lombok.*;

import java.util.List;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TopVideoStatisticsResponseDto {

    private List<VideoStatisticDto> daily;
    private List<VideoStatisticDto> weekly;
    private List<VideoStatisticDto> monthly;
}
