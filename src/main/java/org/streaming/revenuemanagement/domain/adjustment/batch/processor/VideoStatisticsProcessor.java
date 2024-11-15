package org.streaming.revenuemanagement.domain.adjustment.batch.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import org.streaming.revenuemanagement.domain.adjustment.batch.VideoDailyStatisticsMap;
import org.streaming.revenuemanagement.domain.videodailystatistics.entity.VideoDailyStatistics;
import org.streaming.revenuemanagement.domain.videostatistics.entity.VideoStatistics;

@Slf4j
@Component
@RequiredArgsConstructor
public class VideoStatisticsProcessor implements ItemProcessor<VideoStatistics, VideoDailyStatistics> {

    private final VideoDailyStatisticsMap videoDailyStatisticsMap;

    @Override
    public VideoDailyStatistics process(VideoStatistics videoStatistics) throws Exception {

        // VideoDailyStatistics 객체 생성
        VideoDailyStatistics videoDailyStatistics = VideoDailyStatistics.builder()
                .videoStatistics(videoStatistics)
                .videoId(videoStatistics.getVideo().getId())
                .build();

        // statisticsMap에 추가
        videoDailyStatisticsMap.getStatisticsMap().put(videoDailyStatistics.getVideoId(), videoDailyStatistics);
        return videoDailyStatistics;
    }
}
