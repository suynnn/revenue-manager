package org.streaming.revenuemanagement.domain.videolog.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.streaming.revenuemanagement.domain.videolog.entity.VideoLog;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VideoLogRepository extends JpaRepository<VideoLog, Long> {

    Optional<VideoLog> findFirstByVideoIdAndMemberIdOrderByIdDesc(Long videoId, Long memberId);

    Page<VideoLog> findAllByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
}
