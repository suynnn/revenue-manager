package org.streaming.revenuemanagement.domain.advertisement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.streaming.revenuemanagement.domain.advertisement.entity.Advertisement;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdvertisementRespDto {

    private Long id;
    private String advertisementAddr;
    private String advertisementTitle;
    private Boolean isPrivate;
    private Boolean isDeleted;

    public AdvertisementRespDto(Advertisement advertisement) {
        this.id = advertisement.getId();
        this.advertisementAddr = advertisement.getAdvertisementAddr();
        this.advertisementTitle = advertisement.getAdvertisementTitle();
        this.isPrivate = advertisement.getIsPrivate();
        this.isDeleted = advertisement.getIsDeleted();
    }
}
