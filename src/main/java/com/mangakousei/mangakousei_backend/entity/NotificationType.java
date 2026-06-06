package com.mangakousei.mangakousei_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "notification_types")
    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    @Column(name = "notification_type_id")
    @EqualsAndHashCode.Include
    private Long notificationTypeId;

    @Column(name = "notification_name",nullable = false, unique = true)
    private String notificationTypeName;

}
