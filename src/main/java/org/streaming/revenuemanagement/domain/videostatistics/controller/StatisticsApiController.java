package org.streaming.revenuemanagement.domain.videostatistics.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.streaming.revenuemanagement.domain.videodailystatistics.dto.TopVideoStatisticsResponseDto;
import org.streaming.revenuemanagement.domain.videodailystatistics.dto.VideoStatisticDto;
import org.streaming.revenuemanagement.domain.videodailystatistics.service.VideoLeaderboardService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/statistics")
public class StatisticsApiController {

    private final VideoLeaderboardService videoLeaderboardService;

    @GetMapping("/views/top")
    public ResponseEntity<TopVideoStatisticsResponseDto> getTop5VideosByViews() {
        // 어제를 기준으로 통계를 조회
        List<VideoStatisticDto> dailyTopViews = videoLeaderboardService.getTop5VideosByViews("day");
        List<VideoStatisticDto> weeklyTopViews = videoLeaderboardService.getTop5VideosByViews("week");
        List<VideoStatisticDto> monthlyTopViews = videoLeaderboardService.getTop5VideosByViews("month");

        TopVideoStatisticsResponseDto response = new TopVideoStatisticsResponseDto(dailyTopViews, weeklyTopViews, monthlyTopViews);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/playtime/top")
    public ResponseEntity<TopVideoStatisticsResponseDto> getTop5VideosByPlaytime() {
        // 어제를 기준으로 통계를 조회
        List<VideoStatisticDto> dailyTopPlaytime = videoLeaderboardService.getTop5VideosByPlayTime("day");
        List<VideoStatisticDto> weeklyTopPlaytime = videoLeaderboardService.getTop5VideosByPlayTime("week");
        List<VideoStatisticDto> monthlyTopPlaytime = videoLeaderboardService.getTop5VideosByPlayTime("month");

        TopVideoStatisticsResponseDto response = new TopVideoStatisticsResponseDto(dailyTopPlaytime, weeklyTopPlaytime, monthlyTopPlaytime);
        return ResponseEntity.ok(response);
    }
}
