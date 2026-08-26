package com.smartschool.repository;

import com.smartschool.entity.Fee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeeRepository extends JpaRepository<Fee, Long> {
    List<Fee> findByAcademicYearId(Long academicYearId);
    List<Fee> findBySchoolClassId(Long classId);
}
