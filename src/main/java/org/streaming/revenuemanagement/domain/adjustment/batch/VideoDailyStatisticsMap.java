package org.streaming.revenuemanagement.domain.adjustment.batch;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.streaming.revenuemanagement.domain.videodailystatistics.entity.VideoDailyStatistics;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@Getter
public class VideoDailyStatisticsMap {

    private final ConcurrentHashMap<Long, VideoDailyStatistics> statisticsMap = new ConcurrentHashMap<>();

    // 비디오 로그 데이터를 업데이트하는 메서드
    public void updateStatistics(Long videoId, Long views, Long adViews, Long playTime) {
        statisticsMap.computeIfPresent(videoId, (id, existingStatistics) -> {

            // 기존 통계에 새로운 값을 더합니다.
            existingStatistics.updateStatistics(views, adViews, playTime);
            return existingStatistics;
        });
    }

    // Map 초기화 메서드
    public void clear() {
        statisticsMap.clear();
    }
}
