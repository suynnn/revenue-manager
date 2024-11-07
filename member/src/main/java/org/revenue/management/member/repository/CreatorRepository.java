package org.revenue.management.member.repository;

import org.revenue.management.member.entity.Creator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CreatorRepository extends JpaRepository<Creator, Long> {
}
