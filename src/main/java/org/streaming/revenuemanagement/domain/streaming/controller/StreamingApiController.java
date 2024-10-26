package org.streaming.revenuemanagement.domain.streaming.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.streaming.revenuemanagement.domain.streaming.service.AbusingService;
import org.streaming.revenuemanagement.domain.streaming.service.StreamingService;
import org.streaming.revenuemanagement.domain.videolog.dto.VideoLogReqDto;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/streaming/")
public class StreamingApiController {

    private final StreamingService streamingService;
    private final AbusingService abusingService;

    @PostMapping("/watch")
    public String watchVideo(@RequestBody VideoLogReqDto videoLogReqDto,
                             HttpServletRequest request) {

        if (!abusingService.isAbusingUser(videoLogReqDto, request)) {

            streamingService.handleWatchVideo(videoLogReqDto, request);
        }

        return "OK";
    }

    @PostMapping("/pause")
    public String pauseVideo(@RequestParam("videoId")Long videoId,
                             HttpServletRequest request) {

        streamingService.handlePauseVideo(videoId, request);

        return "pause ok";
    }
}
