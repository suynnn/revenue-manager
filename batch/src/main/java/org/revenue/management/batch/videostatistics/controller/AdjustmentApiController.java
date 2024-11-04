package org.revenue.management.batch.videostatistics.controller;

import lombok.RequiredArgsConstructor;
import org.revenue.management.batch.videodailystatistics.service.VideoLeaderboardService;
import org.revenue.management.batch.videostatistics.dto.AdjustmentPeriodStatisticDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
