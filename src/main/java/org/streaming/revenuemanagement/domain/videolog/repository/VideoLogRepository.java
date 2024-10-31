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

    Optional<VideoLog> findFirstByVideoIdAndMemberIdOrderByIdDesc(Long videoId, Long memberId);

    @Query("SELECT new org.streaming.revenuemanagement.domain.videolog.dto.VideoLogStatisticsRespDto(v.video.id, COUNT(v), SUM(v.adCnt), SUM(v.playTime)) " +
            "FROM VideoLog v " +
            "WHERE v.createdAt BETWEEN :start AND :end " +
            "GROUP BY v.video.id")
    Page<VideoLogStatisticsRespDto> findVideoStatisticsBetweenDates(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);

}
