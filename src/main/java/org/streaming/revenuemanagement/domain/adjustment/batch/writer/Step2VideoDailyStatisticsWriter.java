package org.streaming.revenuemanagement.domain.adjustment.batch.writer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.streaming.revenuemanagement.domain.adjustment.batch.dto.VideoStatisticsUpdateDto;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class Step2VideoDailyStatisticsWriter implements ItemWriter<VideoStatisticsUpdateDto> {

    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void write(Chunk<? extends VideoStatisticsUpdateDto> chunk) throws Exception {
        log.info("Writing {} items to the database in batch.", chunk.size());

        String sql = "UPDATE video_daily_statistics " +
                "SET daily_views = daily_views + ?, " +
                "daily_ad_views = daily_ad_views + ?, " +
                "daily_play_time = daily_play_time + ? " +
                "WHERE video_id = ?";

        // 각 DTO의 필드를 사용하여 batch update 수행
        List<Object[]> batchArgs = chunk.getItems().stream()
                .map(dto -> new Object[]{
                        dto.getViews(),
                        Long.valueOf(dto.getAdViews()),
                        dto.getPlayTime(),
                        dto.getVideoId()
                })
                .toList();

        // JdbcTemplate을 사용하여 batch update 실행
        jdbcTemplate.batchUpdate(sql, batchArgs);
    }
}
