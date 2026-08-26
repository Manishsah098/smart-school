package com.smartschool.repository;

import com.smartschool.entity.Homework;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HomeworkRepository extends JpaRepository<Homework, Long> {
    List<Homework> findBySectionIdOrderByDueDateDesc(Long sectionId);
    List<Homework> findByTeacherIdOrderByDueDateDesc(Long teacherId);
    List<Homework> findBySectionIdAndSubjectIdOrderByDueDateDesc(Long sectionId, Long subjectId);
}
