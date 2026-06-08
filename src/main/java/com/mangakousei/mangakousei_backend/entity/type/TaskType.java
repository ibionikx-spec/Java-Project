package com.mangakousei.mangakousei_backend.entity.type;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "task_type")
@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TaskType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_type_id")
    private Long taskTypeId;

    @Column(name = "task_type_name", nullable = false, length = 255, unique = true)
    private String taskTypeName;
}
