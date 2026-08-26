package com.smartschool.repository;

import com.smartschool.entity.Attendance;
import com.smartschool.entity.enums.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByStudentIdAndDate(Long studentId, LocalDate date);
    List<Attendance> findBySectionIdAndDate(Long sectionId, LocalDate date);
    List<Attendance> findByStudentIdAndDateBetweenOrderByDateAsc(Long studentId, LocalDate startDate, LocalDate endDate);
    List<Attendance> findByStudentIdOrderByDateDesc(Long studentId);
    
    long countByStudentId(Long studentId);
    long countByStudentIdAndStatus(Long studentId, AttendanceStatus status);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.date = :date AND a.status = 'PRESENT'")
    long countPresentOnDate(@Param("date") LocalDate date);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.date = :date")
    long countTotalOnDate(@Param("date") LocalDate date);
}
