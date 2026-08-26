package com.smartschool.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class ExamCreateRequest {

    @NotBlank(message = "Exam name is required")
    private String name;

    private String examType; // Unit Test, Mid-Term, Final, Quarterly

    @NotNull(message = "Academic year ID is required")
    private Long academicYearId;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    private boolean isPublished = false;

    public ExamCreateRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getExamType() { return examType; }
    public void setExamType(String examType) { this.examType = examType; }

    public Long getAcademicYearId() { return academicYearId; }
    public void setAcademicYearId(Long academicYearId) { this.academicYearId = academicYearId; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public boolean isPublished() { return isPublished; }
    public void setPublished(boolean published) { isPublished = published; }
}
