package com.smartschool.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class FeePaymentRequest {

    @NotNull(message = "Student fee ID is required")
    private Long studentFeeId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    private Double amount;

    private String paymentMethod = "CASH"; // CASH, CARD, ONLINE, CHEQUE, UPI
    private String receiptNumber;

    public FeePaymentRequest() {}

    public Long getStudentFeeId() { return studentFeeId; }
    public void setStudentFeeId(Long studentFeeId) { this.studentFeeId = studentFeeId; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }
}
