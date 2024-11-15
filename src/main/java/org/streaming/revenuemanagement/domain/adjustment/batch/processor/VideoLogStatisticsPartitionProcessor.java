package org.streaming.revenuemanagement.domain.adjustment.batch.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import org.streaming.revenuemanagement.domain.adjustment.batch.VideoDailyStatisticsMap;
import org.streaming.revenuemanagement.domain.videolog.entity.VideoLog;

@Slf4j
@Component
@RequiredArgsConstructor
public class VideoLogStatisticsPartitionProcessor implements ItemProcessor<VideoLog, Void> {

    private final VideoDailyStatisticsMap videoDailyStatisticsMap;

    @Override
    public Void process(VideoLog videoLog) throws Exception {
        Long videoId = videoLog.getVideo().getId();
        log.info("Processing log for Video ID {}: Ad Count {}, Play Time {}", videoId, videoLog.getAdCnt(), videoLog.getPlayTime());

        // VideoDailyStatisticsMap에 데이터를 업데이트합니다.
        videoDailyStatisticsMap.updateStatistics(videoId, 1L, Long.valueOf(videoLog.getAdCnt()), videoLog.getPlayTime());

        // null을 반환하여 Writer에서 중복 저장하지 않도록 함
        return null;
    }
}