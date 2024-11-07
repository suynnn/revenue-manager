package org.revenue.management.streaming.video.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.revenue.management.streaming.video.entity.Video;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {
}
