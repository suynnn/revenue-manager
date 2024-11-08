package org.streaming.revenuemanagement.domain.adjustment.batch.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import org.streaming.revenuemanagement.domain.videodailystatistics.entity.VideoDailyStatistics;
import org.streaming.revenuemanagement.domain.videodailystatistics.repository.VideoDailyStatisticsRepository;
import org.streaming.revenuemanagement.domain.videolog.dto.VideoLogStatisticsRespDto;
import org.streaming.revenuemanagement.domain.videolog.repository.VideoLogRepository;
import org.streaming.revenuemanagement.domain.videostatistics.entity.VideoStatistics;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class VideoLogStatisticsProcessor implements ItemProcessor<VideoStatistics, VideoDailyStatistics> {

    private final VideoLogRepository videoLogRepository;
    private final VideoDailyStatisticsRepository videoDailyStatisticsRepository;

    @Override
    public VideoDailyStatistics process(VideoStatistics videoStatistics) throws Exception {
        Long videoId = videoStatistics.getVideo().getId();
        LocalDateTime startOfYesterday = LocalDateTime.now().minusDays(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfYesterday = LocalDateTime.now().minusDays(1).withHour(23).withMinute(59).withSecond(59);

        Optional<VideoLogStatisticsRespDto> videoLogStats = videoLogRepository.findVideoStatisticsByVideoIdAndDateRange(videoId, startOfYesterday, endOfYesterday);

        if (videoLogStats.isPresent()) {
            VideoLogStatisticsRespDto stats = videoLogStats.get();
            log.info("Found log stats for Video ID {}: Views {}, Ad Views {}, Play Time {}", videoId, stats.getViews(), stats.getAdViews(), stats.getPlayTime());
            VideoDailyStatistics videoDailyStatistics = videoDailyStatisticsRepository.findByVideoId(videoId)
                    .orElseGet(() -> VideoDailyStatistics.builder().videoId(videoId).build());
            videoDailyStatistics.updateStatistics(stats.getViews(), stats.getAdViews(), stats.getPlayTime());
            return videoDailyStatistics;
        } else {
            log.info("No log stats found for Video ID: {}", videoId);
        }
        return null;
    }
}
