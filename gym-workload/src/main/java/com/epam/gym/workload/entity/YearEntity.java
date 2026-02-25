package com.epam.gym.workload.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "years")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YearEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int yearNumber;

    @ManyToOne
    @JoinColumn(name = "trainer_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private TrainerEntity trainer;

    @Builder.Default
    @OneToMany(mappedBy = "year", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MonthEntity> months = new HashSet<>();
}
