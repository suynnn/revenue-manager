package org.streaming.revenuemanagement.domain.streaming.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.streaming.revenuemanagement.domain.streaming.util.UserUtils;
import org.streaming.revenuemanagement.domain.videolog.dto.VideoLogReqDto;
import org.streaming.revenuemanagement.domain.videolog.service.VideoLogService;

@Slf4j
@Service
@RequiredArgsConstructor
public class StreamingService {

    private final VideoLogService videoLogService;
    private final UserUtils userUtils;

    public void handleWatchVideo(VideoLogReqDto videoLogReqDto, HttpServletRequest request) {
        String userId = userUtils.getUserId(request);

        saveWatchLog(videoLogReqDto, userId);

        printWatchLog(videoLogReqDto, userId);
    }

    private void saveWatchLog(VideoLogReqDto videoLogReqDto, String userId) {

        String redisKey = "log:video:" + videoLogReqDto.getVideoId() + ":user:" + userId;
        videoLogService.videoLogSaveToRedis(redisKey, videoLogReqDto);
    }

    private void printWatchLog(VideoLogReqDto videoLogReqDto, String userId) {

        log.info("User ID: {}, Video ID: {}, Start Time: {}, End Time: {}, Play Time: {}",
                userId, videoLogReqDto.getVideoId(), videoLogReqDto.getStartWatchTime(), videoLogReqDto.getEndWatchTime(), videoLogReqDto.getPlayTime());
    }
}
