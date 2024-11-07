package org.revenue.management.batch.videodailystatistics.repository;

import org.revenue.management.batch.videodailystatistics.dto.VideoStatisticDto;
import org.revenue.management.batch.videodailystatistics.entity.VideoDailyStatistics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VideoDailyStatisticsRepository extends JpaRepository<VideoDailyStatistics, Long> {

    Optional<VideoDailyStatistics> findByVideoId(Long videoId);

    // 특정 기간 동안 생성된 VideoDailyStatistics 엔티티들을 페이징 형식으로 조회
    Page<VideoDailyStatistics> findAllByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    // 조회수가 높은 TOP 5 동영상 가져오기
    // VideoStatisticDto 객체로 비디오 ID와 조회수 합계를 반환하며, 조회수를 기준으로 내림차순으로 정렬
    @Query("SELECT new org.revenue.management.batch.videodailystatistics.dto.VideoStatisticDto(v.videoId, SUM(v.dailyViews)) " +
            "FROM VideoDailyStatistics v " +
            "WHERE v.createdAt BETWEEN :start AND :end " +
            "GROUP BY v.videoId " +
            "ORDER BY SUM(v.dailyViews) DESC")
    List<VideoStatisticDto> findTop5ByViews(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable);

    // 재생 시간이 긴 TOP 5 동영상 가져오기
    // VideoStatisticDto 객체로 비디오 ID와 재생 시간 합계를 반환하며, 재생 시간을 기준으로 내림차순으로 정렬
    @Query("SELECT new org.revenue.management.batch.videodailystatistics.dto.VideoStatisticDto(v.videoId, SUM(v.dailyPlayTime)) " +
            "FROM VideoDailyStatistics v " +
            "WHERE v.createdAt BETWEEN :start AND :end " +
            "GROUP BY v.videoId " +
            "ORDER BY SUM(v.dailyPlayTime) DESC")
    List<VideoStatisticDto> findTop5ByPlayTime(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable);
}
