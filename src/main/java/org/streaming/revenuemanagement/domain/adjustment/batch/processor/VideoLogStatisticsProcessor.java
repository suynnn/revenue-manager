package org.streaming.revenuemanagement.domain.adjustment.batch.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import org.streaming.revenuemanagement.domain.adjustment.batch.dto.VideoStatisticsUpdateDto;
import org.streaming.revenuemanagement.domain.videolog.entity.VideoLog;

@Slf4j
@Component
@RequiredArgsConstructor
public class VideoLogStatisticsProcessor implements ItemProcessor<VideoLog, VideoStatisticsUpdateDto> {

    @Override
    public VideoStatisticsUpdateDto process(VideoLog videoLog) throws Exception {
        Long videoId = videoLog.getVideo().getId();
        log.info("Processing log for Video ID {}: Ad Count {}, Play Time {}", videoId, videoLog.getAdCnt(), videoLog.getPlayTime());

        // DTO에 필요한 통계 데이터를 담아서 반환
        return new VideoStatisticsUpdateDto(videoId, 1L, Long.valueOf(videoLog.getAdCnt()), videoLog.getPlayTime());
    }
}
