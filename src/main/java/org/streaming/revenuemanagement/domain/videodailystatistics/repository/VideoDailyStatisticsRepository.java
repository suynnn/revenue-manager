package org.streaming.revenuemanagement.domain.videodailystatistics.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.streaming.revenuemanagement.domain.videodailystatistics.entity.VideoDailyStatistics;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VideoDailyStatisticsRepository extends JpaRepository<VideoDailyStatistics, Long> {

    Optional<VideoDailyStatistics> findByVideoId(Long videoId);

    Page<VideoDailyStatistics> findAllByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
}
