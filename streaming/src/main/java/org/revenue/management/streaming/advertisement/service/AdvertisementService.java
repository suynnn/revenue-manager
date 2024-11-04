package org.revenue.management.streaming.advertisement.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.revenue.management.streaming.advertisement.dto.AdvertisementRespDto;
import org.revenue.management.streaming.advertisement.repository.AdvertisementRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdvertisementService {

    private final AdvertisementRepository advertisementRepository;

    public List<AdvertisementRespDto> findAll() {

        return advertisementRepository.findAll().stream().map(AdvertisementRespDto::new).toList();
    }
}
