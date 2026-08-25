package com.smartschool.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "sections")
public class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private SchoolClass schoolClass;

    @Column(name = "section_name", nullable = false, length = 20)
    private String sectionName; // e.g. A, B, C

    @Column(name = "room_number", length = 50)
    private String roomNumber;

    @Column(name = "capacity")
    private Integer capacity = 40;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_teacher_id")
    private Teacher classTeacher;

    public Section() {}

    public Section(SchoolClass schoolClass, String sectionName, String roomNumber, Integer capacity, Teacher classTeacher) {
        this.schoolClass = schoolClass;
        this.sectionName = sectionName;
        this.roomNumber = roomNumber;
        this.capacity = capacity;
        this.classTeacher = classTeacher;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public SchoolClass getSchoolClass() { return schoolClass; }
    public void setSchoolClass(SchoolClass schoolClass) { this.schoolClass = schoolClass; }

    public String getSectionName() { return sectionName; }
    public void setSectionName(String sectionName) { this.sectionName = sectionName; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public Teacher getClassTeacher() { return classTeacher; }
    public void setClassTeacher(Teacher classTeacher) { this.classTeacher = classTeacher; }

    public String getFullName() {
        if (schoolClass != null) {
            return schoolClass.getClassName() + " - " + sectionName;
        }
        return sectionName;
    }
}
