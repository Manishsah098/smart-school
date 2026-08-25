package com.smartschool.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "subjects")
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subject_name", nullable = false, length = 100)
    private String subjectName; // e.g. Mathematics

    @Column(name = "subject_code", nullable = false, unique = true, length = 20)
    private String subjectCode; // e.g. MATH101

    public Subject() {}

    public Subject(String subjectName, String subjectCode) {
        this.subjectName = subjectName;
        this.subjectCode = subjectCode;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }
}
