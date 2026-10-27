package com.aicte.apaarverify.repository;

import com.aicte.apaarverify.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentRepository extends JpaRepository<Student, UUID> {
    Optional<Student> findByApaarId(String apaarId);
    List<Student> findAllByApaarId(String apaarId);

    @Query("SELECT s FROM Student s WHERE s.apaarId IS NULL OR s.apaarId = ''")
    List<Student> findStudentsWithMissingApaar();

    @Query("SELECT s FROM Student s WHERE s.apaarFormatValid = false")
    List<Student> findStudentsWithInvalidApaarFormat();
}
