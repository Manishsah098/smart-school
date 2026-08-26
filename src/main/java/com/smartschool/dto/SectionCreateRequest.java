package com.smartschool.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SectionCreateRequest {

    @NotNull(message = "Class ID is required")
    private Long classId;

    @NotBlank(message = "Section name is required")
    private String sectionName;

    private String roomNumber;
    private Integer capacity = 40;
    private Long classTeacherId;

    public SectionCreateRequest() {}

    public Long getClassId() { return id(); }
    public Long id() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }

    public String getSectionName() { return sectionName; }
    public void setSectionName(String sectionName) { this.sectionName = sectionName; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public Long getClassTeacherId() { return classTeacherId; }
    public void setClassTeacherId(Long classTeacherId) { this.classTeacherId = classTeacherId; }
}
