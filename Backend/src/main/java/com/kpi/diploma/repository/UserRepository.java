package com.kpi.diploma.repository;

import com.kpi.diploma.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    @Query("select u.username from User u where u.email = :email")
    Optional<String> findUsernameByEmail(String email);
}
