package com.smartschool.dto;

import com.smartschool.entity.enums.AttendanceStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public class AttendanceBatchRequest {

    @NotNull(message = "Section ID is required")
    private Long sectionId;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotEmpty(message = "Attendance list cannot be empty")
    private List<StudentAttendanceItem> items;

    public static class StudentAttendanceItem {
        private Long studentId;
        private AttendanceStatus status;
        private String remarks;

        public StudentAttendanceItem() {}

        public StudentAttendanceItem(Long studentId, AttendanceStatus status, String remarks) {
            this.studentId = studentId;
            this.status = status;
            this.remarks = remarks;
        }

        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }

        public AttendanceStatus getStatus() { return status; }
        public void setStatus(AttendanceStatus status) { this.status = status; }

        public String getRemarks() { return remarks; }
        public void setRemarks(String remarks) { this.remarks = remarks; }
    }

    public AttendanceBatchRequest() {}

    public Long getSectionId() { return sectionId; }
    public void setSectionId(Long sectionId) { this.sectionId = sectionId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public List<StudentAttendanceItem> getItems() { return items; }
    public void setItems(List<StudentAttendanceItem> items) { this.items = items; }
}
