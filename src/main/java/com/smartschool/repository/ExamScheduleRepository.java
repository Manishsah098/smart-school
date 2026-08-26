package com.smartschool.repository;

import com.smartschool.entity.ExamSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamScheduleRepository extends JpaRepository<ExamSchedule, Long> {
    List<ExamSchedule> findByExamIdOrderByExamDateAscStartTimeAsc(Long examId);
    List<ExamSchedule> findBySectionIdOrderByExamDateAscStartTimeAsc(Long sectionId);
    List<ExamSchedule> findByExamIdAndSectionId(Long examId, Long sectionId);
    Optional<ExamSchedule> findByExamIdAndSectionIdAndSubjectId(Long examId, Long sectionId, Long subjectId);
}
