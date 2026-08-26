package com.smartschool.dto;

import java.util.List;

public class AdminDashboardDTO {
    private long totalStudents;
    private long totalTeachers;
    private long totalParents;
    private long totalClasses;
    private double todayAttendancePercentage;
    private double totalPendingFees;
    private double totalCollectedFees;
    private long upcomingExamsCount;
    private long activeNoticesCount;
    private List<NoticeResponseDTO> recentNotices;
    private List<AuditLogDTO> recentAuditLogs;

    public AdminDashboardDTO() {}

    public long getTotalStudents() { return totalStudents; }
    public void setTotalStudents(long totalStudents) { this.totalStudents = totalStudents; }

    public long getTotalTeachers() { return totalTeachers; }
    public void setTotalTeachers(long totalTeachers) { this.totalTeachers = totalTeachers; }

    public long getTotalParents() { return totalParents; }
    public void setTotalParents(long totalParents) { this.totalParents = totalParents; }

    public long getTotalClasses() { return totalClasses; }
    public void setTotalClasses(long totalClasses) { this.totalClasses = totalClasses; }

    public double getTodayAttendancePercentage() { return todayAttendancePercentage; }
    public void setTodayAttendancePercentage(double todayAttendancePercentage) { this.todayAttendancePercentage = todayAttendancePercentage; }

    public double getTotalPendingFees() { return totalPendingFees; }
    public void setTotalPendingFees(double totalPendingFees) { this.totalPendingFees = totalPendingFees; }

    public double getTotalCollectedFees() { return totalCollectedFees; }
    public void setTotalCollectedFees(double totalCollectedFees) { this.totalCollectedFees = totalCollectedFees; }

    public long getUpcomingExamsCount() { return upcomingExamsCount; }
    public void setUpcomingExamsCount(long upcomingExamsCount) { this.upcomingExamsCount = upcomingExamsCount; }

    public long getActiveNoticesCount() { return activeNoticesCount; }
    public void setActiveNoticesCount(long activeNoticesCount) { this.activeNoticesCount = activeNoticesCount; }

    public List<NoticeResponseDTO> getRecentNotices() { return recentNotices; }
    public void setRecentNotices(List<NoticeResponseDTO> recentNotices) { this.recentNotices = recentNotices; }

    public List<AuditLogDTO> getRecentAuditLogs() { return recentAuditLogs; }
    public void setRecentAuditLogs(List<AuditLogDTO> recentAuditLogs) { this.recentAuditLogs = recentAuditLogs; }
}
