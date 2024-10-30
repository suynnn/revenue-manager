package org.streaming.revenuemanagement.domain.videodailystatistics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.streaming.revenuemanagement.domain.videodailystatistics.dto.VideoStatisticDto;
import org.streaming.revenuemanagement.domain.videodailystatistics.repository.VideoDailyStatisticsRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoLeaderboardService {

    private final VideoDailyStatisticsRepository videoDailyStatisticsRepository;

    public List<VideoStatisticDto> getTop5VideosByViews(String period) {
        LocalDateTime[] dateRange = calculateDateRange(period);

        return videoDailyStatisticsRepository.findTop5ByViews(dateRange[0], dateRange[1], PageRequest.of(0, 5));
    }

    public List<VideoStatisticDto> getTop5VideosByPlayTime(String period) {
        LocalDateTime[] dateRange = calculateDateRange(period);
        return videoDailyStatisticsRepository.findTop5ByPlayTime(dateRange[0], dateRange[1], PageRequest.of(0, 5));
    }

    // 기간에 따른 날짜 범위를 계산하는 메서드
    private LocalDateTime[] calculateDateRange(String period) {
        LocalDateTime start;
        LocalDateTime end;
        LocalDate today = LocalDate.now();

        switch (period) {
            case "day":
                // 오늘의 시작부터 끝까지
                start = today.atStartOfDay();
                end = today.atTime(23, 59, 59);
                break;
            case "week":
                // 오늘이 월요일이면 이전 주 데이터를 가져오기
                if (today.getDayOfWeek() == java.time.DayOfWeek.MONDAY) {
                    start = today.minusWeeks(1).with(java.time.DayOfWeek.MONDAY).atStartOfDay();
                    end = today.minusWeeks(1).with(java.time.DayOfWeek.SUNDAY).atTime(23, 59, 59);
                } else {
                    start = today.with(java.time.DayOfWeek.MONDAY).atStartOfDay();
                    end = today.with(java.time.DayOfWeek.SUNDAY).atTime(23, 59, 59);
                }
                break;
            case "month":
                // 오늘이 달의 첫째 날이면 이전 달 데이터를 가져오기
                if (today.getDayOfMonth() == 1) {
                    start = today.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();
                    end = today.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59);
                } else {
                    start = today.with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();
                    end = today.with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59);
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid period: " + period);
        }
        return new LocalDateTime[]{start, end};
    }
}
