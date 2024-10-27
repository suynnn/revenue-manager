package org.streaming.revenuemanagement.domain.advertisement.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.streaming.revenuemanagement.domain.advertisement.dto.AdvertisementRespDto;
import org.streaming.revenuemanagement.domain.advertisement.repository.AdvertisementRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdvertisementService {

    private final AdvertisementRepository advertisementRepository;

    public List<AdvertisementRespDto> findAll() {

        return advertisementRepository.findAll().stream().map(AdvertisementRespDto::new).toList();
    }
}
