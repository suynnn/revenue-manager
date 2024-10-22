package org.streaming.revenuemanagement.domain.advertisement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.streaming.revenuemanagement.domain.global.entity.BaseEntity;

@Entity
@Table
@Getter
@NoArgsConstructor
public class Advertisement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String advertisementAddr;

    @Column(nullable = false)
    private String advertisementTitle;

    @Column(nullable = false)
    private Boolean isPrivate;

    @Column(nullable = false)
    private Boolean isDeleted = false;

}
