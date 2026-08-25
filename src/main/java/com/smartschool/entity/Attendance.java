package com.smartschool.entity;

import com.smartschool.entity.enums.AttendanceStatus;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "attendance", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"student_id", "date"})
})
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AttendanceStatus status;

    @Column(name = "remarks", length = 255)
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marked_by_teacher_id")
    private Teacher markedByTeacher;

    public Attendance() {}

    public Attendance(Student student, Section section, LocalDate date, AttendanceStatus status, String remarks, Teacher markedByTeacher) {
        this.student = student;
        this.section = section;
        this.date = date;
        this.status = status;
        this.remarks = remarks;
        this.markedByTeacher = markedByTeacher;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public Section getSection() { return section; }
    public void setSection(Section section) { this.section = section; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus status) { this.status = status; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public Teacher getMarkedByTeacher() { return markedByTeacher; }
    public void setMarkedByTeacher(Teacher markedByTeacher) { this.markedByTeacher = markedByTeacher; }
}
