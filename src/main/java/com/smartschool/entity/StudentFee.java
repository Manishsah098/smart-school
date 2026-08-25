package com.smartschool.entity;

import com.smartschool.entity.enums.FeeStatus;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "student_fees", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"fee_id", "student_id"})
})
public class StudentFee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fee_id", nullable = false)
    private Fee fee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    @Column(name = "paid_amount", nullable = false)
    private Double paidAmount = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private FeeStatus status = FeeStatus.PENDING;

    @OneToMany(mappedBy = "studentFee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FeePayment> payments = new ArrayList<>();

    public StudentFee() {}

    public StudentFee(Fee fee, Student student, Double totalAmount) {
        this.fee = fee;
        this.student = student;
        this.totalAmount = totalAmount;
        this.paidAmount = 0.0;
        this.status = FeeStatus.PENDING;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Fee getFee() { return fee; }
    public void setFee(Fee fee) { this.fee = fee; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public Double getPaidAmount() { return paidAmount; }
    public void setPaidAmount(Double paidAmount) { this.paidAmount = paidAmount; }

    public FeeStatus getStatus() { return status; }
    public void setStatus(FeeStatus status) { this.status = status; }

    public List<FeePayment> getPayments() { return payments; }
    public void setPayments(List<FeePayment> payments) { this.payments = payments; }

    public Double getPendingAmount() {
        return Math.max(0.0, totalAmount - (paidAmount != null ? paidAmount : 0.0));
    }
}
