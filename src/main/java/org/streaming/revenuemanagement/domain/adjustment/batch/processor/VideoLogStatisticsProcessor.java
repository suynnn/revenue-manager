package org.streaming.revenuemanagement.domain.adjustment.batch.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import org.streaming.revenuemanagement.domain.videodailystatistics.entity.VideoDailyStatistics;
import org.streaming.revenuemanagement.domain.videodailystatistics.repository.VideoDailyStatisticsRepository;
import org.streaming.revenuemanagement.domain.videolog.entity.VideoLog;

@Slf4j
@Component
@RequiredArgsConstructor
public class VideoLogStatisticsProcessor implements ItemProcessor<VideoLog, VideoDailyStatistics> {

    private final VideoDailyStatisticsRepository videoDailyStatisticsRepository;

    @Override
    public VideoDailyStatistics process(VideoLog videoLog) throws Exception {
        Long videoId = videoLog.getVideo().getId();
        log.info("Processing log for Video ID {}: Ad Count {}, Play Time {}", videoId, videoLog.getAdCnt(), videoLog.getPlayTime());

        // 현재 비디오 ID에 해당하는 VideoDailyStatistics 조회 또는 새로 생성
        VideoDailyStatistics videoDailyStatistics = videoDailyStatisticsRepository.findByVideoId(videoId)
                .orElseGet(() -> VideoDailyStatistics.builder().videoId(videoId).build());

        // 현재 VideoLog의 정보를 기반으로 VideoDailyStatistics 업데이트
        videoDailyStatistics.updateStatistics(1L, Long.valueOf(videoLog.getAdCnt()), videoLog.getPlayTime());

        return videoDailyStatistics;
    }
}
