package org.streaming.revenuemanagement.domain.videolog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.streaming.revenuemanagement.domain.videolog.dto.VideoLogReqDto;
import org.streaming.revenuemanagement.domain.videolog.dto.VideoLogRespDto;
import org.streaming.revenuemanagement.domain.videolog.entity.VideoLog;
import org.streaming.revenuemanagement.domain.videolog.repository.VideoLogRepository;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VideoLogService {

    private final VideoLogRepository videoLogRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String REDIS_KEY_PREFIX = "log:video:";

    public VideoLogRespDto findFirstVideoLogByVideoIdAndMemberId(Long videoId, Long memberId) {

        // Redis 키 생성
        String redisKey = REDIS_KEY_PREFIX + videoId + ":user:" + memberId;

        // Redis에서 데이터 조회
        VideoLogRespDto cachedLog = (VideoLogRespDto) redisTemplate.opsForValue().get(redisKey);
        if (cachedLog != null) {
            log.info("Cache hit for key: {}", redisKey);
            return cachedLog; // Redis에 데이터가 있으면 반환
        }

        // Redis에 데이터가 없으면 DB에서 조회
        Optional<VideoLog> videoLog =
                videoLogRepository.findFirstByVideoIdAndMemberIdOrderByIdDesc(videoId, memberId);

        if (videoLog.isPresent()) {
            log.info("Cache miss for key: {}", redisKey);
            VideoLogRespDto videoLogRespDto = new VideoLogRespDto(videoLog.get());

            // 조회한 데이터를 Redis에 저장
            redisTemplate.opsForValue().set(redisKey, videoLogRespDto);
            return videoLogRespDto;
        }

        // 데이터베이스에도 없으면 null 반환
        return null;
    }

    public void videoLogSaveToRedis(String key, VideoLogReqDto value) {
        redisTemplate.opsForValue().set(key, value);
    }

}
