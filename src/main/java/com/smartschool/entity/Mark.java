package com.smartschool.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "marks", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"exam_schedule_id", "student_id"})
})
public class Mark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_schedule_id", nullable = false)
    private ExamSchedule examSchedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "marks_obtained", nullable = false)
    private Double marksObtained;

    @Column(name = "grade", length = 10)
    private String grade;

    @Column(name = "remarks", length = 255)
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entered_by_teacher_id")
    private Teacher enteredByTeacher;

    public Mark() {}

    public Mark(ExamSchedule examSchedule, Student student, Double marksObtained, String grade, String remarks, Teacher enteredByTeacher) {
        this.examSchedule = examSchedule;
        this.student = student;
        this.marksObtained = marksObtained;
        this.grade = grade;
        this.remarks = remarks;
        this.enteredByTeacher = enteredByTeacher;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ExamSchedule getExamSchedule() { return examSchedule; }
    public void setExamSchedule(ExamSchedule examSchedule) { this.examSchedule = examSchedule; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public Double getMarksObtained() { return marksObtained; }
    public void setMarksObtained(Double marksObtained) { this.marksObtained = marksObtained; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public Teacher getEnteredByTeacher() { return enteredByTeacher; }
    public void setEnteredByTeacher(Teacher enteredByTeacher) { this.enteredByTeacher = enteredByTeacher; }
}
