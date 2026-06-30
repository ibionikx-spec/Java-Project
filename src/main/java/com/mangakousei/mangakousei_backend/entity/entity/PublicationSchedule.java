// src/main/java/com/mangakousei/mangakousei_backend/entity/entity/PublicationSchedule.java
package com.mangakousei.mangakousei_backend.entity.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "publication_schedules")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PublicationSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    @EqualsAndHashCode.Include
    private Long scheduleId;

    // Quan hệ 1-1 với Series
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "series_id", nullable = false, unique = true)
    private Series series;

    // "weekly" hoặc "monthly"
    @Column(name = "schedule_type", nullable = false, length = 20)
    private String scheduleType;

    // Nếu scheduleType = "weekly": 1=Thứ 2, 2=Thứ 3, ... 7=Chủ Nhật
    // Nếu scheduleType = "monthly": 1-31
    @Column(name = "day_value", nullable = false)
    private Integer dayValue;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
