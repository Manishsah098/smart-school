package com.smartschool.repository;

import com.smartschool.entity.FeePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeePaymentRepository extends JpaRepository<FeePayment, Long> {
    List<FeePayment> findByStudentFeeId(Long studentFeeId);
    Optional<FeePayment> findByReceiptNumber(String receiptNumber);
}
