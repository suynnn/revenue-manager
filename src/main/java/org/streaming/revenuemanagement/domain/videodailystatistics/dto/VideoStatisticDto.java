package org.streaming.revenuemanagement.domain.videodailystatistics.dto;

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
