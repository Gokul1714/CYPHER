package com.aicte.apaarverify.repository;

import com.aicte.apaarverify.model.Institution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InstitutionRepository extends JpaRepository<Institution, UUID> {
    Optional<Institution> findByAicteCode(String aicteCode);
}
