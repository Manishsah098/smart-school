package com.smartschool.dto;

import java.util.List;

public class StudentDashboardDTO {
    private StudentResponseDTO profile;
    private double attendancePercentage;
    private boolean lowAttendanceWarning;
    private double averageMarksPercentage;
    private long pendingHomeworkCount;
    private long upcomingExamsCount;
    private double pendingFeeAmount;
    private long unreadNoticesCount;
    private List<HomeworkResponseDTO> recentHomework;
    private List<NoticeResponseDTO> notices;
    private List<TimetableEntryDTO> todaysTimetable;

    public StudentDashboardDTO() {}

    public StudentResponseDTO getProfile() { return profile; }
    public void setProfile(StudentResponseDTO profile) { this.profile = profile; }

    public double getAttendancePercentage() { return attendancePercentage; }
    public void setAttendancePercentage(double attendancePercentage) { this.attendancePercentage = attendancePercentage; }

    public boolean isLowAttendanceWarning() { return lowAttendanceWarning; }
    public void setLowAttendanceWarning(boolean lowAttendanceWarning) { this.lowAttendanceWarning = lowAttendanceWarning; }

    public double getAverageMarksPercentage() { return averageMarksPercentage; }
    public void setAverageMarksPercentage(double averageMarksPercentage) { this.averageMarksPercentage = averageMarksPercentage; }

    public long getPendingHomeworkCount() { return pendingHomeworkCount; }
    public void setPendingHomeworkCount(long pendingHomeworkCount) { this.pendingHomeworkCount = pendingHomeworkCount; }

    public long getUpcomingExamsCount() { return upcomingExamsCount; }
    public void setUpcomingExamsCount(long upcomingExamsCount) { this.upcomingExamsCount = upcomingExamsCount; }

    public double getPendingFeeAmount() { return pendingFeeAmount; }
    public void setPendingFeeAmount(double pendingFeeAmount) { this.pendingFeeAmount = pendingFeeAmount; }

    public long getUnreadNoticesCount() { return unreadNoticesCount; }
    public void setUnreadNoticesCount(long unreadNoticesCount) { this.unreadNoticesCount = unreadNoticesCount; }

    public List<HomeworkResponseDTO> getRecentHomework() { return recentHomework; }
    public void setRecentHomework(List<HomeworkResponseDTO> recentHomework) { this.recentHomework = recentHomework; }

    public List<NoticeResponseDTO> getNotices() { return notices; }
    public void setNotices(List<NoticeResponseDTO> notices) { this.notices = notices; }

    public List<TimetableEntryDTO> getTodaysTimetable() { return todaysTimetable; }
    public void setTodaysTimetable(List<TimetableEntryDTO> todaysTimetable) { this.todaysTimetable = todaysTimetable; }
}
