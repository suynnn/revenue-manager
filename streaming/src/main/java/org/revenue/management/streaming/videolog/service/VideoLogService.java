package org.revenue.management.streaming.videolog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.revenue.management.streaming.videolog.dto.VideoLogReqDto;
import org.revenue.management.streaming.videolog.dto.VideoLogRespDto;
import org.revenue.management.streaming.videolog.entity.VideoLog;
import org.revenue.management.streaming.videolog.repository.VideoLogRepository;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VideoLogService {

    private final VideoLogRepository videoLogRepository;

    private final RedisTemplate<String, Object> redisTemplate;

    public VideoLogRespDto findFirstVideoLogByVideoIdAndMemberId(Long videoId, Long memberId) {

        Optional<VideoLog> videoLog =
                videoLogRepository.findFirstByVideoIdAndMemberIdOrderByIdDesc(videoId, memberId);

        if (videoLog.isPresent()) {
            return new VideoLogRespDto(videoLog.get());
        } else {
            return null;
        }
    }

    public void videoLogSaveToRedis(String key, VideoLogReqDto value) {
        redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(30));
        redisTemplate.expire(key, Duration.ofSeconds(30)); // TTL 설정
    }

    public void videoLogTTLUpdate(String key) {
        Boolean isExist = redisTemplate.hasKey(key);

        if (isExist) {
            redisTemplate.expire(key, Duration.ofSeconds(30));
        }
    }
}
