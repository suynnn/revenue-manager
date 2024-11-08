package org.streaming.revenuemanagement.domain.adjustment.batch.reader;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.streaming.revenuemanagement.domain.videostatistics.entity.VideoStatistics;
import org.streaming.revenuemanagement.domain.videostatistics.repository.VideoStatisticsRepository;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class VideoStatisticsReader {
    private final VideoStatisticsRepository videoStatisticsRepository;

    public RepositoryItemReader<VideoStatistics> reader(int chunkSize) {
        return new RepositoryItemReaderBuilder<VideoStatistics>()
                .name("videoStatisticsReader")
                .arguments(List.of())
                .pageSize(chunkSize)
                .methodName("findAll")
                .repository(videoStatisticsRepository)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }
}
