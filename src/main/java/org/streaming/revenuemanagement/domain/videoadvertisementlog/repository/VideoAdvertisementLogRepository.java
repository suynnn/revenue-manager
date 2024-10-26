package org.streaming.revenuemanagement.domain.videoadvertisementlog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.streaming.revenuemanagement.domain.videoadvertisementlog.entity.VideoAdvertisementLog;

@Repository
public interface VideoAdvertisementLogRepository extends JpaRepository<VideoAdvertisementLog, Long> {
}
