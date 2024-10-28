package org.streaming.revenuemanagement.domain.videostatistics.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.streaming.revenuemanagement.domain.videostatistics.entity.VideoStatistics;

@Repository
public interface VideoStatisticsRepository extends JpaRepository<VideoStatistics, Long> {
}
