package org.streaming.revenuemanagement.domain.streaming.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.streaming.revenuemanagement.domain.streaming.util.UserUtils;
import org.streaming.revenuemanagement.domain.videolog.dto.VideoLogReqDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class AbusingService {

    private final UserUtils userUtils;
    private final RedisTemplate<String, Object> redisTemplate;

    public boolean isAbusingUser(VideoLogReqDto videoLogReqDto, HttpServletRequest request) {
        String userId = userUtils.getUserId(request);

        // Redis 키 생성
        String redisKey = "log:video:" + videoLogReqDto.getVideoId() + ":user:" + userId;

        // Redis에 키가 존재하면 바로 어뷰징으로 간주
        Boolean keyExists = redisTemplate.hasKey(redisKey);

        if (Boolean.TRUE.equals(keyExists)) {
            return true;
        }

        // CreatorId와 MemberId가 동일하면 어뷰징으로 간주
        if (videoLogReqDto.getCreatorId().equals(videoLogReqDto.getMemberId())) {
            return true;
        }

        return false; // 위 조건에 해당하지 않으면 어뷰징 X
    }

}
