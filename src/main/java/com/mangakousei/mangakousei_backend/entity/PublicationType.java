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
@Table(name = "publication_types")
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PublicationType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "publication_type_id")
    @EqualsAndHashCode.Include
    private Long publicationTypeId;

    @Column(name = "publication_type_name", nullable = false, length = 255, unique = true)
    private String publicationTypeName;    
}
