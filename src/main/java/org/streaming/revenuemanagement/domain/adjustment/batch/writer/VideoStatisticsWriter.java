package org.streaming.revenuemanagement.domain.adjustment.batch.writer;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import org.streaming.revenuemanagement.domain.videostatistics.entity.VideoStatistics;
import org.streaming.revenuemanagement.domain.videostatistics.repository.VideoStatisticsRepository;

@Component
@RequiredArgsConstructor
public class VideoStatisticsWriter implements ItemWriter<VideoStatistics> {

    private final VideoStatisticsRepository videoStatisticsRepository;

    @Override
    public void write(Chunk<? extends VideoStatistics> chunk) throws Exception {
        videoStatisticsRepository.saveAll(chunk);
    }
}
