package org.streaming.revenuemanagement.domain.videoadvertisementlog.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.streaming.revenuemanagement.domain.videoadvertisementlog.repository.VideoAdvertisementLogRepository;

@Service
@RequiredArgsConstructor
public class VideoAdvertisementLogService {

    private final VideoAdvertisementLogRepository videoAdvertisementLogRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public void videoAdvertisementLogSaveToRedis(String key) {
        redisTemplate.opsForValue().increment(key, 1);
    }
}
