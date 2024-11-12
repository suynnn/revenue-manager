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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class VideoLogReader {

    private final VideoLogRepository videoLogRepository;

    @Value("${spring.batch.chunk.size}")
    int chunkSize;

    @Bean
    @StepScope
    public RepositoryItemReader<VideoLog> reader() {

        LocalDateTime start = LocalDateTime.now().minusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = LocalDateTime.now().minusDays(1).withHour(23).withMinute(59).withSecond(59).withNano(0);

        return new RepositoryItemReaderBuilder<VideoLog>()
                .name("videoLogReader")
                .repository(videoLogRepository)
                .methodName("findVideoLogsByDateRange") // 전체 범위 데이터를 가져오는 메서드 사용
                .arguments(Arrays.asList(start, end))
                .sorts(Map.of("id", Sort.Direction.ASC)) // 정렬 조건 추가
                .pageSize(chunkSize)
                .build();
    }
}
