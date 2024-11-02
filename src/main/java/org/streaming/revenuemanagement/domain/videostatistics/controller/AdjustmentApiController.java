package org.streaming.revenuemanagement.domain.videostatistics.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.streaming.revenuemanagement.domain.videodailystatistics.service.VideoLeaderboardService;
import org.streaming.revenuemanagement.domain.videostatistics.dto.AdjustmentPeriodStatisticDto;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/adjustment")
public class AdjustmentApiController {

    private final VideoLeaderboardService videoLeaderboardService;

    @GetMapping("/{videoId}")
    public ResponseEntity<AdjustmentPeriodStatisticDto> getAdjustmentByVideoId(@PathVariable("videoId") Long videoId) {
        AdjustmentPeriodStatisticDto adjustment = videoLeaderboardService.getAdjustmentByVideoId(videoId);

        return ResponseEntity.ok(adjustment);
    }

}
