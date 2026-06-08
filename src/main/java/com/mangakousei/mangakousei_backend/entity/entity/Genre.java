package com.mangakousei.mangakousei_backend.entity.entity;
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
@Table(name = "genres")
public class Genre{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "genre_id")
    @EqualsAndHashCode.Include
    private Long genreId;

    @Column(name = "genre_name", nullable = false, length= 255)
    private String genreName;
}
