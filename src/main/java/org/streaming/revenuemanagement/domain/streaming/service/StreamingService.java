package org.streaming.revenuemanagement.domain.streaming.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.streaming.revenuemanagement.domain.streaming.util.UserUtils;
import org.streaming.revenuemanagement.domain.videoadvertisementlog.service.VideoAdvertisementLogService;
import org.streaming.revenuemanagement.domain.videolog.dto.VideoLogReqDto;
import org.streaming.revenuemanagement.domain.videolog.service.VideoLogService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class StreamingService {

    private final VideoLogService videoLogService;
    private final UserUtils userUtils;
    private final VideoAdvertisementLogService videoAdvertisementLogService;

    public void handlePauseVideo(VideoLogReqDto videoLogReqDto, Long videoId, HttpServletRequest request) {
        String userId = userUtils.getUserId(request);

        String formattedDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String redisKey = "log:video:" + videoId + ":user:" + userId + ":date:" + formattedDate;

        videoLogService.videoLogSaveToRedis(redisKey, videoLogReqDto);
    }

    public void watchAdvertisement(Long advertisementId, Long videoId, HttpServletRequest request) {
        String userId = userUtils.getUserId(request);

        String redisKey = "ad:" + advertisementId + ":video:" + videoId + ":user:" + userId;

        videoAdvertisementLogService.videoAdvertisementLogSaveToRedis(redisKey);
    }
}