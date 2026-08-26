package com.smartschool.repository;

import com.smartschool.entity.HomeworkSubmission;
import com.smartschool.entity.enums.HomeworkStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HomeworkSubmissionRepository extends JpaRepository<HomeworkSubmission, Long> {
    Optional<HomeworkSubmission> findByHomeworkIdAndStudentId(Long homeworkId, Long studentId);
    List<HomeworkSubmission> findByHomeworkId(Long homeworkId);
    List<HomeworkSubmission> findByStudentId(Long studentId);
    long countByStudentIdAndStatus(Long studentId, HomeworkStatus status);
}
