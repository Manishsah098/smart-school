package com.smartschool.repository;

import com.smartschool.entity.TeacherAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherAssignmentRepository extends JpaRepository<TeacherAssignment, Long> {
    List<TeacherAssignment> findByTeacherId(Long teacherId);
    List<TeacherAssignment> findBySectionId(Long sectionId);
    boolean existsByTeacherIdAndSectionId(Long teacherId, Long sectionId);
    Optional<TeacherAssignment> findByTeacherIdAndSectionIdAndSubjectId(Long teacherId, Long sectionId, Long subjectId);
    void deleteByTeacherIdAndSectionIdAndSubjectId(Long teacherId, Long sectionId, Long subjectId);
}
