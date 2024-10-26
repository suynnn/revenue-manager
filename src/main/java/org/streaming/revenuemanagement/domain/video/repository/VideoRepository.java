package org.streaming.revenuemanagement.domain.video.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.streaming.revenuemanagement.domain.video.entity.Video;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {
}
