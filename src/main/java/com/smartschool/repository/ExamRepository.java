package com.smartschool.repository;

import com.smartschool.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {
    List<Exam> findByAcademicYearIdOrderByStartDateAsc(Long academicYearId);
    List<Exam> findByIsPublishedTrueOrderByStartDateDesc();
}
