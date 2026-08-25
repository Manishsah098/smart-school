package com.smartschool.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "classes")
public class SchoolClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "class_name", nullable = false, length = 50)
    private String className; // e.g. Class 10

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;

    @OneToMany(mappedBy = "schoolClass", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Section> sections = new ArrayList<>();

    public SchoolClass() {}

    public SchoolClass(String className, AcademicYear academicYear) {
        this.className = className;
        this.academicYear = academicYear;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public AcademicYear getAcademicYear() { return academicYear; }
    public void setAcademicYear(AcademicYear academicYear) { this.academicYear = academicYear; }

    public List<Section> getSections() { return sections; }
    public void setSections(List<Section> sections) { this.sections = sections; }
}
