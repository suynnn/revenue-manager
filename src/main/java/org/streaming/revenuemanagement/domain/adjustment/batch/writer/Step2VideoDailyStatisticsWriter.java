package org.streaming.revenuemanagement.domain.adjustment.batch.writer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.streaming.revenuemanagement.domain.adjustment.batch.dto.VideoStatisticsUpdateDto;
import org.streaming.revenuemanagement.domain.videodailystatistics.repository.VideoDailyStatisticsRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class Step2VideoDailyStatisticsWriter implements ItemWriter<VideoStatisticsUpdateDto> {

    private final VideoDailyStatisticsRepository videoDailyStatisticsRepository;

    @Override
    @Transactional
    public void write(Chunk<? extends VideoStatisticsUpdateDto> chunk) throws Exception {
        log.info("Writing {} items to the database.", chunk.size());

        // VideoStatisticsUpdateDto를 기반으로 비디오별 통계 업데이트
        for (VideoStatisticsUpdateDto dto : chunk) {
            videoDailyStatisticsRepository.bulkUpdateStatistics(
                    dto.getVideoId(),
                    dto.getViews(),
                    Long.valueOf(dto.getAdViews()),
                    dto.getPlayTime()
            );
        }
    }
}
