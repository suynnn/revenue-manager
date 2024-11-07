package org.revenue.management.batch.videostatistics.repository;

import org.revenue.management.batch.videostatistics.dto.AdjustmentStatisticDto;
import org.revenue.management.batch.videostatistics.entity.VideoStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VideoStatisticsRepository extends JpaRepository<VideoStatistics, Long> {

    // 특정 비디오 ID와 기간을 기준으로 정산 통계 정보를 조회하는 쿼리 메서드
    // 데이터 전체가 아닌 비디오 아이디, 총 조회수 정산, 총 광고 조회수 정산 데이터만 DTO로 반환받음
    @Query("SELECT new org.revenue.management.batch.videostatistics.dto.AdjustmentStatisticDto(v.video.id, v.totalAdjustment, v.totalAdAdjustment) " +
            "FROM VideoStatistics v " +
            "WHERE v.video.id = :videoId AND v.createdAt BETWEEN :start AND :end")
    List<AdjustmentStatisticDto> findAdjustmentStatisticsByVideoIdAndDateRange(@Param("videoId") Long videoId,
                                                                               @Param("start") LocalDateTime start,
                                                                               @Param("end") LocalDateTime end);

}
