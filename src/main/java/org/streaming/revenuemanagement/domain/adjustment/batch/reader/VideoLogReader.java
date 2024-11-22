package org.streaming.revenuemanagement.domain.adjustment.batch.reader;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.streaming.revenuemanagement.domain.videolog.entity.VideoLog;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class VideoLogReader {

    private final EntityManagerFactory entityManagerFactory;

    @Bean
    @StepScope
    public JpaCursorItemReader<VideoLog> reader() {
        LocalDateTime start = LocalDateTime.now().minusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = LocalDateTime.now().minusDays(1).withHour(23).withMinute(59).withSecond(59).withNano(0);

        return new JpaCursorItemReaderBuilder<VideoLog>()
                .name("videoLogReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT v FROM VideoLog v WHERE v.createdAt BETWEEN :start AND :end ORDER BY v.id")
                .parameterValues(Map.of(
                        "start", start,
                        "end", end
                ))
                .saveState(true)
                .build();
    }
}
