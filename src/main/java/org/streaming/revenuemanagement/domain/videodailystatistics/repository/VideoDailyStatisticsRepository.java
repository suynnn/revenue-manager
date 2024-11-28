package org.streaming.revenuemanagement.domain.videodailystatistics.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    // 특정 기간 동안 생성된 VideoDailyStatistics 엔티티들을 페이징 형식으로 조회
    Page<VideoDailyStatistics> findAllByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    // 조회수가 높은 TOP 5 동영상 가져오기
    // VideoStatisticDto 객체로 비디오 ID와 조회수 합계를 반환하며, 조회수를 기준으로 내림차순으로 정렬
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
    // VideoStatisticDto 객체로 비디오 ID와 재생 시간 합계를 반환하며, 재생 시간을 기준으로 내림차순으로 정렬
    @Query("SELECT new org.streaming.revenuemanagement.domain.videodailystatistics.dto.VideoStatisticDto(v.videoId, SUM(v.dailyPlayTime)) " +
            "FROM VideoDailyStatistics v " +
            "WHERE v.createdAt BETWEEN :start AND :end " +
            "GROUP BY v.videoId " +
            "ORDER BY SUM(v.dailyPlayTime) DESC")
    List<VideoStatisticDto> findTop5ByPlayTime(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable);

    Optional<VideoDailyStatistics> findByVideoIdAndCreatedAtBetween(Long videoId, LocalDateTime startOfDay, LocalDateTime endOfDay);

    @Modifying
    @Query("UPDATE VideoDailyStatistics v SET v.dailyViews = v.dailyViews + :views, v.dailyAdViews = v.dailyAdViews + :adViews, v.dailyPlayTime = v.dailyPlayTime + :playTime WHERE v.videoId = :videoId")
    void bulkUpdateStatistics(@Param("videoId") Long videoId, @Param("views") Long views, @Param("adViews") Long adViews, @Param("playTime") Long playTime);

    @Query("SELECT COALESCE(MIN(v.id), 0) FROM VideoDailyStatistics v WHERE v.createdAt BETWEEN :start AND :end")
    long findMinId(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(MAX(v.id), 0) FROM VideoDailyStatistics v WHERE v.createdAt BETWEEN :start AND :end")
    long findMaxId(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
