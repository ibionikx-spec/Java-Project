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

@Entity
@Table(name = "statuses")
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Status {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "status_id")
    @EqualsAndHashCode.Include
    private Long statusId;

    @Column(name = "status_name", nullable = false, length = 255, unique = true)
    private String statusName;
}
