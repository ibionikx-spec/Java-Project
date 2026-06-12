package com.mangakousei.mangakousei_backend.entity.status;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.EqualsAndHashCode;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity @NoArgsConstructor
@Table(name = "manuscript_status")
public class ManuscriptStatus{
    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    @Column(name = "manuscript_status_id")
    @EqualsAndHashCode.Include
    private Long manuscriptTypeId;

    @Column(name = "manuscript_name",nullable = false, unique = true)
    private String manuscriptName;

}
