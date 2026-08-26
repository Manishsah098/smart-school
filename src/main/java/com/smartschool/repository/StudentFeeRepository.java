package com.smartschool.repository;

import com.smartschool.entity.StudentFee;
import com.smartschool.entity.enums.FeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentFeeRepository extends JpaRepository<StudentFee, Long> {
    Optional<StudentFee> findByFeeIdAndStudentId(Long feeId, Long studentId);
    List<StudentFee> findByStudentId(Long studentId);
    List<StudentFee> findByFeeId(Long feeId);
    List<StudentFee> findByStatus(FeeStatus status);

    @Query("SELECT SUM(sf.totalAmount - sf.paidAmount) FROM StudentFee sf WHERE sf.status <> 'PAID'")
    Double sumPendingFees();

    @Query("SELECT SUM(sf.paidAmount) FROM StudentFee sf")
    Double sumCollectedFees();
}
