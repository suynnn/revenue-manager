package org.streaming.revenuemanagement.domain.adjustment.batch.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import org.streaming.revenuemanagement.domain.adjustment.batch.dto.VideoLogAdCntPlayTimeDto;
import org.streaming.revenuemanagement.domain.adjustment.batch.dto.VideoStatisticsUpdateDto;
import org.streaming.revenuemanagement.domain.videodailystatistics.entity.VideoDailyStatistics;
import org.streaming.revenuemanagement.domain.videolog.repository.VideoLogRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class VideoLogStatisticsPartitionProcessor implements ItemProcessor<VideoDailyStatistics, VideoStatisticsUpdateDto> {

    private final VideoLogRepository videoLogRepository;

    @Override
    public VideoStatisticsUpdateDto process(VideoDailyStatistics videoDailyStatistics) throws Exception {
        Long videoId = videoDailyStatistics.getVideoStatistics().getVideo().getId();
        log.info("Processing VideoDailyStatistics for Video ID {}", videoId);

        LocalDateTime startOfDay = LocalDate.now().minusDays(1).atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().minusDays(1).atTime(LocalTime.MAX);

        // 비디오 ID에 해당하는 VideoLog 데이터의 adCnt와 playTime 필드만 가져오기
        List<VideoLogAdCntPlayTimeDto> videoLogDataList = videoLogRepository.findAdCntAndPlayTimeByVideoIdAndCreatedAtBetween(videoId, startOfDay, endOfDay);

        long totalViews = 0;
        long totalAdCnt = 0;
        long totalPlayTime = 0;

        // adCnt와 playTime 누적
        for (VideoLogAdCntPlayTimeDto videoLogData : videoLogDataList) {
            totalViews += 1;
            totalAdCnt += videoLogData.getAdCnt();
            totalPlayTime += videoLogData.getPlayTime();
        }

        return new VideoStatisticsUpdateDto(videoId, totalViews, totalAdCnt, totalPlayTime);
    }
}