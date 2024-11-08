package org.streaming.revenuemanagement.domain.adjustment.batch.reader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.streaming.revenuemanagement.domain.videostatistics.entity.VideoStatistics;
import org.streaming.revenuemanagement.domain.videostatistics.repository.VideoStatisticsRepository;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class VideoStatisticsPartitionReader {

    private final VideoStatisticsRepository videoStatisticsRepository;

    @Bean
    @StepScope
    public RepositoryItemReader<VideoStatistics> reader(@Value("#{stepExecutionContext[minId]}") Long minId,
                                                        @Value("#{stepExecutionContext[maxId]}") Long maxId,
                                                        @Value("${spring.batch.chunk.size}") Integer chunkSize) {
        log.info("Partition Reader initialized with minId: {} and maxId: {}", minId, maxId);
        return new RepositoryItemReaderBuilder<VideoStatistics>()
                .name("videoStatisticsPartitionReader")
                .arguments(List.of(minId, maxId))
                .pageSize(chunkSize)
                .methodName("findByIdBetween")
                .repository(videoStatisticsRepository)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }
}
