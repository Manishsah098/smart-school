package com.smartschool.repository;

import com.smartschool.entity.Student;
import com.smartschool.entity.enums.StudentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByUserId(Long userId);
    Optional<Student> findByStudentId(String studentId);
    Optional<Student> findByAdmissionNumber(String admissionNumber);
    List<Student> findBySectionId(Long sectionId);
    List<Student> findBySectionIdAndStatus(Long sectionId, StudentStatus status);
    List<Student> findByStatus(StudentStatus status);
    
    @Query("SELECT s FROM Student s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(s.studentId) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(s.admissionNumber) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Student> searchStudents(@Param("query") String query);

    @Query("SELECT s FROM Student s WHERE s.section.id = :sectionId AND (LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(s.studentId) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(s.admissionNumber) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Student> searchStudentsInSection(@Param("sectionId") Long sectionId, @Param("query") String query);

    long countBySectionId(Long sectionId);
    long countByStatus(StudentStatus status);
}
