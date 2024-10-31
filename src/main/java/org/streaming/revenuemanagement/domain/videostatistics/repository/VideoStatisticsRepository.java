package org.streaming.revenuemanagement.domain.videostatistics.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.streaming.revenuemanagement.domain.videostatistics.dto.AdjustmentStatisticDto;
import org.streaming.revenuemanagement.domain.videostatistics.entity.VideoStatistics;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VideoStatisticsRepository extends JpaRepository<VideoStatistics, Long> {

    @Query("SELECT new org.streaming.revenuemanagement.domain.videostatistics.dto.AdjustmentStatisticDto(v.video.id, v.totalAdjustment, v.totalAdAdjustment) " +
            "FROM VideoStatistics v " +
            "WHERE v.video.id = :videoId AND v.createdAt BETWEEN :start AND :end")
    List<AdjustmentStatisticDto> findAdjustmentStatisticsByVideoIdAndDateRange(@Param("videoId") Long videoId,
                                                                               @Param("start") LocalDateTime start,
                                                                               @Param("end") LocalDateTime end);

}
