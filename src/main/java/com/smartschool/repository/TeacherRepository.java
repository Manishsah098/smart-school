package com.smartschool.repository;

import com.smartschool.entity.Teacher;
import com.smartschool.entity.enums.TeacherStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    Optional<Teacher> findByUserId(Long userId);
    Optional<Teacher> findByEmployeeId(String employeeId);
    List<Teacher> findByStatus(TeacherStatus status);
}
