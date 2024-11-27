package org.streaming.revenuemanagement.domain.adjustment.batch.writer;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.streaming.revenuemanagement.domain.videodailystatistics.entity.VideoDailyStatistics;
import org.streaming.revenuemanagement.domain.videodailystatistics.repository.VideoDailyStatisticsRepository;
import org.streaming.revenuemanagement.domain.videostatistics.entity.VideoStatistics;
import org.streaming.revenuemanagement.domain.videostatistics.repository.VideoStatisticsRepository;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AdjustmentVideoDailyStatisticsWriter implements ItemWriter<VideoDailyStatistics> {

    private final VideoDailyStatisticsRepository videoDailyStatisticsRepository;
    private final VideoStatisticsRepository videoStatisticsRepository;

    @Override
    @Transactional
    public void write(Chunk<? extends VideoDailyStatistics> chunk) throws Exception {
        List<VideoDailyStatistics> updatedVideoDailyStatistics = new ArrayList<>();
        List<VideoStatistics> updatedVideoStatistics = new ArrayList<>();

        for (VideoDailyStatistics dailyStatistics : chunk) {
            updatedVideoDailyStatistics.add(dailyStatistics);
            VideoStatistics videoStatistics = dailyStatistics.getVideoStatistics();
            updatedVideoStatistics.add(videoStatistics);
        }
        videoDailyStatisticsRepository.saveAll(updatedVideoDailyStatistics);
        videoStatisticsRepository.saveAll(updatedVideoStatistics);
    }
}
