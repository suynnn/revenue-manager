package org.streaming.revenuemanagement.domain.videolog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.streaming.revenuemanagement.domain.videolog.entity.VideoLog;

@Repository
public interface VideoLogRepository extends JpaRepository<VideoLog, Long> {
}
