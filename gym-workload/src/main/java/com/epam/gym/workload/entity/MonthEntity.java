package com.epam.gym.workload.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "months")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int monthNumber;

    @Column(nullable = false)
    private long trainingDuration;

    @ManyToOne
    @JoinColumn(name = "year_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private YearEntity year;
}
