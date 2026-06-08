package com.mangakousei.mangakousei_backend.entity.system;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "notification_types")
public class NotificationType{
    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    @Column(name = "notification_type_id")
    @EqualsAndHashCode.Include
    private Long notificationTypeId;

    @Column(name = "notification_name",nullable = false, unique = true)
    private String notificationTypeName;

}
