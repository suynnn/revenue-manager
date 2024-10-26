package org.streaming.revenuemanagement.domain.video.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.streaming.revenuemanagement.domain.video.dto.VideoRespDto;
import org.streaming.revenuemanagement.domain.video.entity.Video;
import org.streaming.revenuemanagement.domain.video.repository.VideoRepository;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoRepository videoRepository;

    public VideoRespDto findById(Long videoId) {
        Video video = videoRepository.findById(videoId).orElseThrow();

        return new VideoRespDto(video);
    }
}
