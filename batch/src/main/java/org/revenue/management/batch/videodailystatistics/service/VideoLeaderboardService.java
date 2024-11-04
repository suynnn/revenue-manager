package org.revenue.management.batch.videodailystatistics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.revenue.management.batch.videodailystatistics.dto.VideoStatisticDto;
import org.revenue.management.batch.videodailystatistics.repository.VideoDailyStatisticsRepository;
import org.revenue.management.batch.videostatistics.dto.AdjustmentPeriodStatisticDto;
import org.revenue.management.batch.videostatistics.dto.AdjustmentStatisticDto;
import org.revenue.management.batch.videostatistics.repository.VideoStatisticsRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoLeaderboardService {

    private final VideoDailyStatisticsRepository videoDailyStatisticsRepository;
    private final VideoStatisticsRepository videoStatisticsRepository;

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

    public AdjustmentPeriodStatisticDto getAdjustmentByVideoId(Long videoId) {
        LocalDateTime[] dailyRange = calculateDateRange("day");
        LocalDateTime[] weeklyRange = calculateDateRange("week");
        LocalDateTime[] monthlyRange = calculateDateRange("month");

        List<AdjustmentStatisticDto> dailyStatistics = videoStatisticsRepository.findAdjustmentStatisticsByVideoIdAndDateRange(videoId, dailyRange[0], dailyRange[1]);
        List<AdjustmentStatisticDto> weeklyStatistics = videoStatisticsRepository.findAdjustmentStatisticsByVideoIdAndDateRange(videoId, weeklyRange[0], weeklyRange[1]);
        List<AdjustmentStatisticDto> monthlyStatistics = videoStatisticsRepository.findAdjustmentStatisticsByVideoIdAndDateRange(videoId, monthlyRange[0], monthlyRange[1]);

        return new AdjustmentPeriodStatisticDto(videoId, dailyStatistics, weeklyStatistics, monthlyStatistics);
    }

}
