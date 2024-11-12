package org.streaming.revenuemanagement.domain.videolog.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.streaming.revenuemanagement.domain.videolog.dto.VideoLogStatisticsRespDto;
import org.streaming.revenuemanagement.domain.videolog.entity.VideoLog;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VideoLogRepository extends JpaRepository<VideoLog, Long> {

    // 특정 비디오 ID와 회원 ID에 대한 가장 최근의 VideoLog를 조회
    Optional<VideoLog> findFirstByVideoIdAndMemberIdOrderByIdDesc(Long videoId, Long memberId);

    // 특정 기간 동안의 비디오 통계를 조회하여 VideoLogStatisticsRespDto 객체로 반환
    // 비디오별로 광고 수(adCnt), 재생 시간(playTime), 그리고 로그 개수를 그룹화하여 통계 제공
    @Query("SELECT new org.streaming.revenuemanagement.domain.videolog.dto.VideoLogStatisticsRespDto(v.video.id, COUNT(v), SUM(v.adCnt), SUM(v.playTime)) " +
            "FROM VideoLog v " +
            "WHERE v.createdAt BETWEEN :start AND :end " +
            "GROUP BY v.video.id")
    Page<VideoLogStatisticsRespDto> findVideoStatisticsBetweenDates(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);

    @Query("SELECT new org.streaming.revenuemanagement.domain.videolog.dto.VideoLogStatisticsRespDto(v.video.id, COUNT(v), SUM(v.adCnt), SUM(v.playTime)) " +
            "FROM VideoLog v " +
            "WHERE v.video.id = :videoId AND v.createdAt BETWEEN :start AND :end " +
            "GROUP BY v.video.id")
    Optional<VideoLogStatisticsRespDto> findVideoStatisticsByVideoIdAndDateRange(@Param("videoId") Long videoId,
                                                                                 @Param("start") LocalDateTime start,
                                                                                 @Param("end") LocalDateTime end);

    @Query("SELECT v FROM VideoLog v WHERE v.id BETWEEN :minId AND :maxId")
    Page<VideoLog> findVideoLogsByIdRange(@Param("minId") Long minId,
                                                           @Param("maxId") Long maxId,
                                                           Pageable pageable);

    // 특정 기간 동안의 모든 VideoLog를 가져오는 메서드
    @Query("SELECT v FROM VideoLog v WHERE v.createdAt BETWEEN :start AND :end")
    Page<VideoLog> findVideoLogsByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);

    @Query("SELECT COALESCE(MIN(v.id), 0) FROM VideoLog v WHERE v.createdAt BETWEEN :start AND :end")
    long findMinId(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 특정 기간 동안의 최대 VideoLog ID를 조회
    @Query("SELECT COALESCE(MAX(v.id), 0) FROM VideoLog v WHERE v.createdAt BETWEEN :start AND :end")
    long findMaxId(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}

