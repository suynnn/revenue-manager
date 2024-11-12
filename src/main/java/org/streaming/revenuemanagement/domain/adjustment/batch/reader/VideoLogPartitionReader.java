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
import org.streaming.revenuemanagement.domain.videolog.entity.VideoLog;
import org.streaming.revenuemanagement.domain.videolog.repository.VideoLogRepository;

import java.util.Arrays;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class VideoLogPartitionReader {

    private final VideoLogRepository videoLogRepository;

    @Bean
    @StepScope
    public RepositoryItemReader<VideoLog> videoLogPartitionReaderMethod(
            @Value("#{stepExecutionContext['minId']}") Long minId,
            @Value("#{stepExecutionContext['maxId']}") Long maxId,
            @Value("${spring.batch.chunk.size}") Integer chunkSize) {

        log.info("minId, maxId, chunkSize = {}, {}, {}", minId, maxId, chunkSize);

        return new RepositoryItemReaderBuilder<VideoLog>()
                .name("videoLogPartitionReader")
                .repository(videoLogRepository)
                .methodName("findVideoLogsByIdRange")
                .arguments(Arrays.asList(minId, maxId))
                .sorts(Map.of("id", Sort.Direction.ASC)) // 여기에 정렬 조건 추가
                .pageSize(chunkSize)
                .build();
    }
}
