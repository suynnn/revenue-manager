package org.streaming.revenuemanagement.domain.adjustment.batch.reader;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.streaming.revenuemanagement.domain.videodailystatistics.entity.VideoDailyStatistics;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class VideoLogPartitionReader {

    private final EntityManagerFactory entityManagerFactory;

    @Bean
    @StepScope
    public JpaCursorItemReader<VideoDailyStatistics> videoDailyStatisticsPartitionReader(
            @Value("#{stepExecutionContext['minId']}") Long minId,
            @Value("#{stepExecutionContext['maxId']}") Long maxId) {

        log.info("minId, maxId = {}, {}", minId, maxId);

        return new JpaCursorItemReaderBuilder<VideoDailyStatistics>()
                .name("videoDailyStatisticsPartitionReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT v FROM VideoDailyStatistics v WHERE v.id BETWEEN :minId AND :maxId ORDER BY v.id ASC")
                .parameterValues(Map.of(
                        "minId", minId,
                        "maxId", maxId
                ))
                .saveState(true)
                .build();
    }
}
