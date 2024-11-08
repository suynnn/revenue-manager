package org.streaming.revenuemanagement.domain.adjustment.batch.reader;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.streaming.revenuemanagement.domain.videodailystatistics.entity.VideoDailyStatistics;
import org.streaming.revenuemanagement.domain.videodailystatistics.repository.VideoDailyStatisticsRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AdjustmentVideoDailyStatisticsReader {

    private final VideoDailyStatisticsRepository videoDailyStatisticsRepository;

    public RepositoryItemReader<VideoDailyStatistics> reader(int chunkSize) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
        return new RepositoryItemReaderBuilder<VideoDailyStatistics>()
                .name("adjustmentVideoDailyStatisticsReader")
                .arguments(List.of(startOfDay, endOfDay))
                .pageSize(chunkSize)
                .methodName("findAllByCreatedAtBetween")
                .repository(videoDailyStatisticsRepository)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }
}
