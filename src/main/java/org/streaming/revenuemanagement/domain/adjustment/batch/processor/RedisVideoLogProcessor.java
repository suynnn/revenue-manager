package org.streaming.revenuemanagement.domain.adjustment.batch.processor;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import org.streaming.revenuemanagement.domain.member.entity.Member;
import org.streaming.revenuemanagement.domain.member.repository.MemberRepository;
import org.streaming.revenuemanagement.domain.video.entity.Video;
import org.streaming.revenuemanagement.domain.video.repository.VideoRepository;
import org.streaming.revenuemanagement.domain.videolog.dto.VideoLogReqDto;
import org.streaming.revenuemanagement.domain.videolog.entity.VideoLog;

@Component
@RequiredArgsConstructor
public class RedisVideoLogProcessor implements ItemProcessor<VideoLogReqDto, VideoLog> {

    private final VideoRepository videoRepository;
    private final MemberRepository memberRepository;

    @Override
    public VideoLog process(VideoLogReqDto dto) {
        // Video 엔티티 조회
        Video video = videoRepository.findById(dto.getVideoId()).orElseThrow();

        // Member 엔티티 조회
        Member member = null;
        if (dto.getMemberId() != null) {
            member = memberRepository.findById(dto.getMemberId()).orElseThrow();
        }

        // 엔티티 변환
        return member != null ? new VideoLog(dto, video, member) : new VideoLog(dto, video);
    }

}
