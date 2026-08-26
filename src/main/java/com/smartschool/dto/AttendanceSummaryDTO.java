package com.smartschool.dto;

import java.util.List;

public class AttendanceSummaryDTO {
    private Long studentId;
    private String studentName;
    private long totalWorkingDays;
    private long presentDays;
    private long absentDays;
    private long lateDays;
    private long excusedDays;
    private double percentage;
    private boolean lowAttendanceWarning;
    private List<AttendanceRecordDTO> recentRecords;

    public AttendanceSummaryDTO() {}

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public long getTotalWorkingDays() { return totalWorkingDays; }
    public void setTotalWorkingDays(long totalWorkingDays) { this.totalWorkingDays = totalWorkingDays; }

    public long getPresentDays() { return presentDays; }
    public void setPresentDays(long presentDays) { this.presentDays = presentDays; }

    public long getAbsentDays() { return absentDays; }
    public void setAbsentDays(long absentDays) { this.absentDays = absentDays; }

    public long getLateDays() { return lateDays; }
    public void setLateDays(long lateDays) { this.lateDays = lateDays; }

    public long getExcusedDays() { return excusedDays; }
    public void setExcusedDays(long excusedDays) { this.excusedDays = excusedDays; }

    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }

    public boolean isLowAttendanceWarning() { return lowAttendanceWarning; }
    public void setLowAttendanceWarning(boolean lowAttendanceWarning) { this.lowAttendanceWarning = lowAttendanceWarning; }

    public List<AttendanceRecordDTO> getRecentRecords() { return recentRecords; }
    public void setRecentRecords(List<AttendanceRecordDTO> recentRecords) { this.recentRecords = recentRecords; }
}
