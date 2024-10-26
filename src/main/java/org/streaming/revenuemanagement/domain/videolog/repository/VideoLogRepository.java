package org.streaming.revenuemanagement.domain.videolog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.streaming.revenuemanagement.domain.videolog.entity.VideoLog;

import java.util.Optional;

@Repository
public interface VideoLogRepository extends JpaRepository<VideoLog, Long> {

    Optional<VideoLog> findFirstByVideoIdAndMemberIdOrderByIdDesc(Long videoId, Long memberId);
}
