package com.mangakousei.mangakousei_backend.entity.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.mangakousei.mangakousei_backend.entity.type.RegionType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "page_region")
@Getter @Setter @NoArgsConstructor @Builder @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PageRegion {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   @Column(name = "region_id")
   @EqualsAndHashCode.Include
   private Long regionId;

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name ="page_id", nullable = false)
   @JsonBackReference("PageRegion")
   private Page page;

   @Column(name = "x")
   private BigDecimal x;

   @Column(name = "y")
   private BigDecimal y;
   
   @Column(name = "height")
   private BigDecimal height;

   @Column(name = "width")
   private BigDecimal width;

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name ="region_type_id", nullable= false)
   private RegionType regionType;

   @Column(name = "note", columnDefinition = "TEXT")
   private String note;

   @OneToMany(mappedBy = "region", cascade = CascadeType.ALL, orphanRemoval = true)
   @JsonManagedReference("RegionTask")
   @Builder.Default
   private List<Task> tasks = new ArrayList<>();
}
