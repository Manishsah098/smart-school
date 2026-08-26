package com.smartschool.dto;

import java.util.List;

public class TeacherDashboardDTO {
    private Long teacherId;
    private String teacherName;
    private String employeeId;
    private List<SectionResponseDTO> assignedClasses;
    private long totalStudents;
    private long pendingHomeworkReviews;
    private long upcomingExamsCount;
    private List<NoticeResponseDTO> notices;

    public TeacherDashboardDTO() {}

    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public List<SectionResponseDTO> getAssignedClasses() { return assignedClasses; }
    public void setAssignedClasses(List<SectionResponseDTO> assignedClasses) { this.assignedClasses = assignedClasses; }

    public long getTotalStudents() { return totalStudents; }
    public void setTotalStudents(long totalStudents) { this.totalStudents = totalStudents; }

    public long getPendingHomeworkReviews() { return pendingHomeworkReviews; }
    public void setPendingHomeworkReviews(long pendingHomeworkReviews) { this.pendingHomeworkReviews = pendingHomeworkReviews; }

    public long getUpcomingExamsCount() { return upcomingExamsCount; }
    public void setUpcomingExamsCount(long upcomingExamsCount) { this.upcomingExamsCount = upcomingExamsCount; }

    public List<NoticeResponseDTO> getNotices() { return notices; }
    public void setNotices(List<NoticeResponseDTO> notices) { this.notices = notices; }
}
