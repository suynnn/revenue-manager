package org.streaming.revenuemanagement.domain.streaming.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.streaming.revenuemanagement.domain.member.entity.Member;
import org.streaming.revenuemanagement.domain.member.repository.MemberRepository;
import org.streaming.revenuemanagement.domain.video.entity.Video;
import org.streaming.revenuemanagement.domain.video.repository.VideoRepository;
import org.streaming.revenuemanagement.domain.videolog.dto.VideoLogReqDto;
import org.streaming.revenuemanagement.domain.videolog.entity.VideoLog;
import org.streaming.revenuemanagement.domain.videolog.repository.VideoLogRepository;

import java.util.LinkedHashMap;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StreamingScheduler {

    private final RedisTemplate<String, Object> redisTemplate;

    private final VideoLogRepository videoLogRepository;
    private final VideoRepository videoRepository;
    private final MemberRepository memberRepository;

    ObjectMapper objectMapper = new ObjectMapper();

    @Scheduled(fixedRate = 10000) // 5초마다 실행
    public void checkAndSaveLogs() {
        Set<String> keys = redisTemplate.keys("log:video:*");

        if (keys != null) {
            for (String key : keys) {
                Long ttl = redisTemplate.getExpire(key);

                if (ttl != null && ttl <= 10) { // 만료까지 10초 이하 남은 경우
                    LinkedHashMap map = (LinkedHashMap) redisTemplate.opsForValue().get(key);

                    // ObjectMapper를 사용하여 DTO로 변환
                    VideoLogReqDto videoLogReqDto = objectMapper.convertValue(map, VideoLogReqDto.class);

                    if (videoLogReqDto != null) {

                        Video video = videoRepository.findById(videoLogReqDto.getVideoId()).orElseThrow();

                        if (key.contains("ip")) {
                            videoLogReqDto.setGuestIp(key.split(" ")[1]);
                            VideoLog videoLog = new VideoLog(videoLogReqDto, video);

                            videoLogRepository.save(videoLog);
                        }
                        else {
                            Member member = memberRepository.findById(videoLogReqDto.getMemberId()).orElseThrow();

                            VideoLog videoLog = new VideoLog(videoLogReqDto, video, member);

                            videoLogRepository.save(videoLog);
                        }
                        redisTemplate.delete(key); // 저장 후 Redis에서 삭제

                        System.out.println("DB에 저장되었습니다: " + videoLogReqDto);
                    }
                }
            }
        }
    }

}
