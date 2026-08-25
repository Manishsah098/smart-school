package com.smartschool.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "academic_years")
public class AcademicYear {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "year_name", nullable = false, unique = true, length = 20)
    private String yearName; // e.g. 2026-2027

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "is_current", nullable = false)
    private boolean isCurrent = false;

    public AcademicYear() {}

    public AcademicYear(String yearName, LocalDate startDate, LocalDate endDate, boolean isCurrent) {
        this.yearName = yearName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isCurrent = isCurrent;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getYearName() { return yearName; }
    public void setYearName(String yearName) { this.yearName = yearName; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public boolean isCurrent() { return isCurrent; }
    public void setCurrent(boolean current) { isCurrent = current; }
}
