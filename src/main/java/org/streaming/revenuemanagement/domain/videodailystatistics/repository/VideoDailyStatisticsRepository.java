package org.streaming.revenuemanagement.domain.videodailystatistics.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.streaming.revenuemanagement.domain.videodailystatistics.dto.VideoStatisticDto;
import org.streaming.revenuemanagement.domain.videodailystatistics.entity.VideoDailyStatistics;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VideoDailyStatisticsRepository extends JpaRepository<VideoDailyStatistics, Long> {

    Optional<VideoDailyStatistics> findByVideoId(Long videoId);

    Page<VideoDailyStatistics> findAllByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    // 조회수가 높은 TOP 5 동영상 가져오기
    @Query("SELECT new org.streaming.revenuemanagement.domain.videodailystatistics.dto.VideoStatisticDto(v.videoId, SUM(v.dailyViews)) " +
            "FROM VideoDailyStatistics v " +
            "WHERE v.createdAt BETWEEN :start AND :end " +
            "GROUP BY v.videoId " +
            "ORDER BY SUM(v.dailyViews) DESC")
    List<VideoStatisticDto> findTop5ByViews(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable);

    // 재생 시간이 긴 TOP 5 동영상 가져오기
    @Query("SELECT new org.streaming.revenuemanagement.domain.videodailystatistics.dto.VideoStatisticDto(v.videoId, SUM(v.dailyPlayTime)) " +
            "FROM VideoDailyStatistics v " +
            "WHERE v.createdAt BETWEEN :start AND :end " +
            "GROUP BY v.videoId " +
            "ORDER BY SUM(v.dailyPlayTime) DESC")
    List<VideoStatisticDto> findTop5ByPlayTime(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable);
}
