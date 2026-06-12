package com.mangakousei.mangakousei_backend.entity.type;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "manuscript_type")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ManuscriptType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "manuscript_type_id")
    @EqualsAndHashCode.Include
    private Long manuscriptTypeId;

    @Column(name = "type_name", nullable = false, unique = true)
    private String typeName;
}