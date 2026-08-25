package com.smartschool.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "exams")
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;

    @Column(name = "name", nullable = false, length = 100)
    private String name; // e.g. Mid-Term Examination 2026

    @Column(name = "exam_type", length = 50)
    private String examType; // Unit Test, Mid-Term, Final, Quarterly

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "is_published", nullable = false)
    private boolean isPublished = false;

    public Exam() {}

    public Exam(AcademicYear academicYear, String name, String examType, LocalDate startDate, LocalDate endDate, boolean isPublished) {
        this.academicYear = academicYear;
        this.name = name;
        this.examType = examType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isPublished = isPublished;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public AcademicYear getAcademicYear() { return academicYear; }
    public void setAcademicYear(AcademicYear academicYear) { this.academicYear = academicYear; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getExamType() { return examType; }
    public void setExamType(String examType) { this.examType = examType; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public boolean isPublished() { return isPublished; }
    public void setPublished(boolean published) { isPublished = published; }
}
