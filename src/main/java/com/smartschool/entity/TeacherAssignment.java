package com.smartschool.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "teacher_assignments")
public class TeacherAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @Column(name = "is_class_teacher", nullable = false)
    private boolean isClassTeacher = false;

    public TeacherAssignment() {}

    public TeacherAssignment(Teacher teacher, Section section, Subject subject, boolean isClassTeacher) {
        this.teacher = teacher;
        this.section = section;
        this.subject = subject;
        this.isClassTeacher = isClassTeacher;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Teacher getTeacher() { return teacher; }
    public void setTeacher(Teacher teacher) { this.teacher = teacher; }

    public Section getSection() { return section; }
    public void setSection(Section section) { this.section = section; }

    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }

    public boolean isClassTeacher() { return isClassTeacher; }
    public void setClassTeacher(boolean classTeacher) { isClassTeacher = classTeacher; }
}
