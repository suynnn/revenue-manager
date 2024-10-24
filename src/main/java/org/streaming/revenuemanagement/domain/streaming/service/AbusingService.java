package org.streaming.revenuemanagement.domain.streaming.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.streaming.revenuemanagement.domain.streaming.util.UserUtils;
import org.streaming.revenuemanagement.domain.videolog.dto.VideoLogReqDto;

import java.util.LinkedHashMap;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class AbusingService {

    private final UserUtils userUtils;
    private final RedisTemplate<String, Object> redisTemplate;

    ObjectMapper objectMapper = new ObjectMapper();

    public boolean isAbusingUser(VideoLogReqDto videoLogReqDto, HttpServletRequest request) {
        String userId = userUtils.getUserId(request);

        String redisKey = "log:video:" + videoLogReqDto.getVideoId() + ":user:" + userId;

        Boolean keyExists = redisTemplate.hasKey(redisKey);

        LinkedHashMap map = (LinkedHashMap) redisTemplate.opsForValue().get(redisKey);

        // ObjectMapper를 사용하여 DTO로 변환
        VideoLogReqDto videoLogReqDtoInRedis = objectMapper.convertValue(map, VideoLogReqDto.class);

        // 존재 여부를 로깅
        if (keyExists) {
            if (!Objects.equals(videoLogReqDto.getUuid(), videoLogReqDtoInRedis.getUuid())) {
                log.info("이미 시청중인 유저 입니다. 어뷰징으로 의심됩니다.");
                return true;
            }
        }
        else if (videoLogReqDto.getCreatorId() == videoLogReqDto.getMemberId()) {
            log.info("The user is the creator of this video.");
            return true;
        }

        log.info("This user is not abusing.");
        return false; // 키가 없으면 남용 아님
    }

}
