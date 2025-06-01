package com.kpi.diploma.repository;

import com.kpi.diploma.entity.Sharing;
import com.kpi.diploma.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SharingRepository extends JpaRepository<Sharing, Long> {
    List<Sharing> findByTarget(User user);
    Optional<Sharing> findByExerciseIdAndOwnerAndTarget(Long id, User owner, User target);
}
