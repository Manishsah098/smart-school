package com.smartschool.repository;

import com.smartschool.entity.ParentStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParentStudentRepository extends JpaRepository<ParentStudent, Long> {
    List<ParentStudent> findByParentId(Long parentId);
    List<ParentStudent> findByStudentId(Long studentId);
    boolean existsByParentIdAndStudentId(Long parentId, Long studentId);
    Optional<ParentStudent> findByParentIdAndStudentId(Long parentId, Long studentId);
}
