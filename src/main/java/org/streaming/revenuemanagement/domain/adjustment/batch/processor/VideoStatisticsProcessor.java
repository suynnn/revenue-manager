package org.streaming.revenuemanagement.domain.adjustment.batch.processor;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import org.streaming.revenuemanagement.domain.videodailystatistics.entity.VideoDailyStatistics;
import org.streaming.revenuemanagement.domain.videostatistics.entity.VideoStatistics;

@Component
@RequiredArgsConstructor
public class VideoStatisticsProcessor implements ItemProcessor<VideoStatistics, VideoDailyStatistics> {

    @Override
    public VideoDailyStatistics process(VideoStatistics videoStatistics) throws Exception {
        return VideoDailyStatistics.builder()
                .videoStatistics(videoStatistics)
                .videoId(videoStatistics.getVideo().getId())
                .build();
    }
}
