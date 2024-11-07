package org.revenue.management.streaming.videoadvertisementlog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.revenue.management.streaming.videoadvertisementlog.entity.VideoAdvertisementLog;

@Repository
public interface VideoAdvertisementLogRepository extends JpaRepository<VideoAdvertisementLog, Long> {
}
