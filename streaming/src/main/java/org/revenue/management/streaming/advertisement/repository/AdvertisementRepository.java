package org.revenue.management.streaming.advertisement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.revenue.management.streaming.advertisement.entity.Advertisement;

@Repository
public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {
}
