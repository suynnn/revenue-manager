package org.revenue.management.streaming.video.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.revenue.management.streaming.video.dto.VideoRespDto;
import org.revenue.management.streaming.video.entity.Video;
import org.revenue.management.streaming.video.repository.VideoRepository;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoRepository videoRepository;

    public VideoRespDto findById(Long videoId) {
        Video video = videoRepository.findById(videoId).orElseThrow();

        return new VideoRespDto(video);
    }
}
