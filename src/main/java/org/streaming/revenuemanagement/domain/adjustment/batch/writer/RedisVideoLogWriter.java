package org.streaming.revenuemanagement.domain.adjustment.batch.writer;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.streaming.revenuemanagement.domain.videolog.entity.VideoLog;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RedisVideoLogWriter implements ItemWriter<VideoLog> {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void write(Chunk<? extends VideoLog> items) {
        String sql = "INSERT INTO video_logs (video_id, member_id, guest_ip, start_watch_time, end_watch_time, " +
                "play_time, is_watch_completed, ad_cnt, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, now(), now())";

        List<Object[]> batchArgs = items.getItems().stream() // Chunk에서 List를 가져옴
                .map(log -> new Object[]{
                        log.getVideo().getId(),
                        log.getMember() != null ? log.getMember().getId() : null,
                        log.getGuestIp(),
                        log.getStartWatchTime(),
                        log.getEndWatchTime(),
                        log.getPlayTime(),
                        log.getIsWatchCompleted(),
                        log.getAdCnt()
                })
                .collect(Collectors.toList());

        jdbcTemplate.batchUpdate(sql, batchArgs);
    }
}
