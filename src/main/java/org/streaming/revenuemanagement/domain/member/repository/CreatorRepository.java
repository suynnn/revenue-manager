package org.streaming.revenuemanagement.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.streaming.revenuemanagement.domain.member.entity.Creator;

@Repository
public interface CreatorRepository extends JpaRepository<Creator, Long> {
}
