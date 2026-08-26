package com.smartschool.dto;

import com.smartschool.entity.enums.FeeStatus;
import com.smartschool.entity.enums.FeeType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class StudentFeeDTO {
    private Long id;
    private Long feeId;
    private String title;
    private FeeType feeType;
    private LocalDate dueDate;
    private Long studentId;
    private String studentName;
    private String admissionNumber;
    private Double totalAmount;
    private Double paidAmount;
    private Double pendingAmount;
    private FeeStatus status;
    private List<PaymentRecordDTO> paymentHistory;

    public static class PaymentRecordDTO {
        private Long id;
        private LocalDateTime paymentDate;
        private Double amount;
        private String paymentMethod;
        private String receiptNumber;
        private String receivedBy;

        public PaymentRecordDTO() {}

        public PaymentRecordDTO(Long id, LocalDateTime paymentDate, Double amount, String paymentMethod, String receiptNumber, String receivedBy) {
            this.id = id;
            this.paymentDate = paymentDate;
            this.amount = amount;
            this.paymentMethod = paymentMethod;
            this.receiptNumber = receiptNumber;
            this.receivedBy = receivedBy;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public LocalDateTime getPaymentDate() { return paymentDate; }
        public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }

        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }

        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

        public String getReceiptNumber() { return receiptNumber; }
        public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }

        public String getReceivedBy() { return receivedBy; }
        public void setReceivedBy(String receivedBy) { this.receivedBy = receivedBy; }
    }

    public StudentFeeDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getFeeId() { return feeId; }
    public void setFeeId(Long feeId) { this.feeId = feeId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public FeeType getFeeType() { return feeType; }
    public void setFeeType(FeeType feeType) { this.feeType = feeType; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getAdmissionNumber() { return admissionNumber; }
    public void setAdmissionNumber(String admissionNumber) { this.admissionNumber = admissionNumber; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public Double getPaidAmount() { return paidAmount; }
    public void setPaidAmount(Double paidAmount) { this.paidAmount = paidAmount; }

    public Double getPendingAmount() { return pendingAmount; }
    public void setPendingAmount(Double pendingAmount) { this.pendingAmount = pendingAmount; }

    public FeeStatus getStatus() { return status; }
    public void setStatus(FeeStatus status) { this.status = status; }

    public List<PaymentRecordDTO> getPaymentHistory() { return paymentHistory; }
    public void setPaymentHistory(List<PaymentRecordDTO> paymentHistory) { this.paymentHistory = paymentHistory; }
}
