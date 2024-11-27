package org.streaming.revenuemanagement.domain.adjustment.batch.writer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.streaming.revenuemanagement.domain.adjustment.batch.VideoDailyStatisticsMap;
import org.streaming.revenuemanagement.domain.videodailystatistics.entity.VideoDailyStatistics;
import org.streaming.revenuemanagement.domain.videodailystatistics.repository.VideoDailyStatisticsRepository;

import java.util.Collection;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class VideoDailyStatisticsPartitionWriter implements ItemWriter<VideoDailyStatistics> {

    private final VideoDailyStatisticsRepository videoDailyStatisticsRepository;
    private final VideoDailyStatisticsMap videoDailyStatisticsMap;

    @Override
    @Transactional
    public void write(Chunk<? extends VideoDailyStatistics> chunk) throws Exception {
        // 통합된 statisticsMap에서 모든 VideoDailyStatistics 객체를 가져옴
        Map<Long, VideoDailyStatistics> statisticsMap = videoDailyStatisticsMap.getStatisticsMap();
        Collection<VideoDailyStatistics> aggregatedStatistics = statisticsMap.values();

        if (!aggregatedStatistics.isEmpty()) {
            // 데이터베이스에 저장
            videoDailyStatisticsRepository.saveAll(aggregatedStatistics);

            // 저장 후 맵 초기화
            statisticsMap.clear();
        }
    }
}