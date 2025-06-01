package com.kpi.diploma.repository;

import com.kpi.diploma.entity.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
    List<Exercise> findByOwnerId(Long id);
    Optional<Exercise> findByName(String name);
    List<Exercise> findByOwnerIdAndName(Long id, String name);
}
