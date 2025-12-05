package com.epam.gym.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Reference data entity for training types. This table contains a constant list of values and
 * should not be modified from the application.
 */
@Entity
@Table(name = "training_types")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
public class TrainingType {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @ToString.Include
  @EqualsAndHashCode.Include
  private Long id;

  @Column(name = "name", nullable = false, unique = true, length = 50)
  @ToString.Include
  private String name;

  @OneToMany(mappedBy = "specialization", fetch = FetchType.LAZY)
  private List<Trainer> trainers;

  @OneToMany(mappedBy = "specialization", fetch = FetchType.LAZY)
  private List<Training> trainings;

  /**
   * Enum representing the constant training type values. Use this in business logic for type-safe
   * comparisons.
   */
  public enum Type {
    CARDIO,
    STRENGTH,
    YOGA,
    HIIT,
    PILATES
  }
}
