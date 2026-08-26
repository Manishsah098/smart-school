package com.smartschool.dto;

import com.smartschool.entity.enums.FeeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class FeeCreateRequest {

    @NotBlank(message = "Fee title is required")
    private String title;

    @NotNull(message = "Academic year ID is required")
    private Long academicYearId;

    private Long classId; // Nullable if school-wide

    @NotNull(message = "Fee type is required")
    private FeeType feeType = FeeType.TUITION;

    @NotNull(message = "Amount is required")
    private Double amount;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    public FeeCreateRequest() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Long getAcademicYearId() { return academicYearId; }
    public void setAcademicYearId(Long academicYearId) { this.academicYearId = academicYearId; }

    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }

    public FeeType getFeeType() { return feeType; }
    public void setFeeType(FeeType feeType) { this.feeType = feeType; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
}
