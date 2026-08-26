package com.smartschool.repository;

import com.smartschool.entity.AcademicYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AcademicYearRepository extends JpaRepository<AcademicYear, Long> {
    Optional<AcademicYear> findByIsCurrentTrue();
    Optional<AcademicYear> findByYearName(String yearName);
}
