package com.aicte.apaarverify.repository;

import com.aicte.apaarverify.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByStaffId(String staffId);
    boolean existsByStaffId(String staffId);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
