package com.smartschool.repository;

import com.smartschool.entity.Mark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MarkRepository extends JpaRepository<Mark, Long> {
    Optional<Mark> findByExamScheduleIdAndStudentId(Long examScheduleId, Long studentId);
    List<Mark> findByExamScheduleId(Long examScheduleId);
    List<Mark> findByStudentId(Long studentId);
    List<Mark> findByStudentIdAndExamScheduleExamId(Long studentId, Long examId);
}
