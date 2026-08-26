package com.smartschool.service;

import com.smartschool.dto.*;
import com.smartschool.entity.*;
import com.smartschool.entity.enums.DayOfWeek;
import com.smartschool.entity.enums.FeeStatus;
import com.smartschool.entity.enums.NoticeAudience;
import com.smartschool.exception.ResourceNotFoundException;
import com.smartschool.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final AttendanceService attendanceService;
    private final HomeworkService homeworkService;
    private final ExamResultService examResultService;
    private final FeeService feeService;
    private final TimetableService timetableService;
    private final NoticeNotificationService noticeService;
    private final AdminService adminService;
    private final ExamRepository examRepository;
    private final TimetableRepository timetableRepository;
    private final StudentFeeRepository studentFeeRepository;

    public StudentService(StudentRepository studentRepository,
                          AttendanceService attendanceService,
                          HomeworkService homeworkService,
                          ExamResultService examResultService,
                          FeeService feeService,
                          TimetableService timetableService,
                          NoticeNotificationService noticeService,
                          AdminService adminService,
                          ExamRepository examRepository,
                          TimetableRepository timetableRepository,
                          StudentFeeRepository studentFeeRepository) {
        this.studentRepository = studentRepository;
        this.attendanceService = attendanceService;
        this.homeworkService = homeworkService;
        this.examResultService = examResultService;
        this.feeService = feeService;
        this.timetableService = timetableService;
        this.noticeService = noticeService;
        this.adminService = adminService;
        this.examRepository = examRepository;
        this.timetableRepository = timetableRepository;
        this.studentFeeRepository = studentFeeRepository;
    }

    @Transactional(readOnly = true)
    public Student getStudentByUserId(Long userId) {
        return studentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user ID: " + userId));
    }

    @Transactional(readOnly = true)
    public StudentDashboardDTO getDashboard(Long studentUserId) {
        Student student = getStudentByUserId(studentUserId);

        StudentDashboardDTO dto = new StudentDashboardDTO();
        dto.setProfile(adminService.convertStudentToDTO(student));

        AttendanceSummaryDTO attSummary = attendanceService.getStudentAttendanceSummary(student.getId());
        dto.setAttendancePercentage(attSummary.getPercentage());
        dto.setLowAttendanceWarning(attSummary.isLowAttendanceWarning());

        dto.setAverageMarksPercentage(examResultService.getStudentAverageMarks(student.getId()));

        List<HomeworkResponseDTO> homeworkList = homeworkService.getHomeworkForStudent(student.getId());
        dto.setRecentHomework(homeworkList.stream().limit(5).collect(Collectors.toList()));
        long pendingHw = homeworkList.stream()
                .filter(hw -> hw.getSubmissionStatus() == null || hw.getSubmissionStatus().name().equals("PENDING"))
                .count();
        dto.setPendingHomeworkCount(pendingHw);

        dto.setUpcomingExamsCount(examRepository.count());

        List<StudentFeeDTO> fees = feeService.getStudentFees(student.getId());
        double pendingFee = fees.stream()
                .filter(f -> f.getStatus() != FeeStatus.PAID)
                .mapToDouble(StudentFeeDTO::getPendingAmount)
                .sum();
        dto.setPendingFeeAmount(pendingFee);

        List<NoticeResponseDTO> notices = noticeService.getNoticesForAudienceAndSection(NoticeAudience.STUDENTS, student.getSection().getId());
        dto.setNotices(notices.stream().limit(5).collect(Collectors.toList()));
        dto.setUnreadNoticesCount(notices.size());

        DayOfWeek currentDay = DayOfWeek.valueOf(LocalDate.now().getDayOfWeek().name());
        List<TimetableEntryDTO> todaysSchedule = timetableRepository
                .findBySectionIdAndDayOfWeekOrderByStartTimeAsc(student.getSection().getId(), currentDay)
                .stream()
                .map(timetableService::convertToDTO)
                .collect(Collectors.toList());
        dto.setTodaysTimetable(todaysSchedule);

        return dto;
    }

    @Transactional(readOnly = true)
    public StudentResponseDTO getProfile(Long studentUserId) {
        Student student = getStudentByUserId(studentUserId);
        return adminService.convertStudentToDTO(student);
    }
}
