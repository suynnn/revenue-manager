package org.revenue.management.batch.videodailystatistics.dto;

import lombok.*;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VideoStatisticDto {

    private Long videoId;
    private Long totalValue;
}
