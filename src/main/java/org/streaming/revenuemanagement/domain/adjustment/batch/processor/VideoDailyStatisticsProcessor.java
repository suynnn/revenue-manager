package org.streaming.revenuemanagement.domain.adjustment.batch.processor;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import org.streaming.revenuemanagement.domain.videodailystatistics.entity.VideoDailyStatistics;
import org.streaming.revenuemanagement.domain.videostatistics.entity.VideoStatistics;
import org.streaming.revenuemanagement.domain.videostatistics.repository.VideoStatisticsRepository;

@Component
@RequiredArgsConstructor
public class VideoDailyStatisticsProcessor implements ItemProcessor<VideoDailyStatistics, VideoStatistics> {

    private final VideoStatisticsRepository videoStatisticsRepository;

    @Override
    public VideoStatistics process(VideoDailyStatistics videoDailyStatistics) throws Exception {
        VideoStatistics videoStatistics = videoStatisticsRepository.findById(videoDailyStatistics.getVideoStatistics().getId())
                .orElseThrow();
        videoStatistics.updateStatistics(
                videoDailyStatistics.getDailyViews(),
                videoDailyStatistics.getDailyAdViews(),
                videoDailyStatistics.getDailyPlayTime()
        );
        return videoStatistics;
    }
}
