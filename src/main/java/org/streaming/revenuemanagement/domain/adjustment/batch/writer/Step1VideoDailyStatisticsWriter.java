package org.streaming.revenuemanagement.domain.adjustment.batch.writer;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import org.streaming.revenuemanagement.domain.videodailystatistics.entity.VideoDailyStatistics;
import org.streaming.revenuemanagement.domain.videodailystatistics.repository.VideoDailyStatisticsRepository;

@Component
@RequiredArgsConstructor
public class Step1VideoDailyStatisticsWriter implements ItemWriter<VideoDailyStatistics> {

    private final VideoDailyStatisticsRepository videoDailyStatisticsRepository;

    @Override
    public void write(Chunk<? extends VideoDailyStatistics> chunk) throws Exception {
        videoDailyStatisticsRepository.saveAll(chunk);
    }
}
