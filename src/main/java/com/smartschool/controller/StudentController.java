package com.smartschool.controller;

import com.smartschool.dto.*;
import com.smartschool.entity.Exam;
import com.smartschool.entity.Student;
import com.smartschool.entity.enums.NoticeAudience;
import com.smartschool.repository.ExamRepository;
import com.smartschool.security.UserPrincipal;
import com.smartschool.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student")
@PreAuthorize("hasAnyAuthority('ROLE_STUDENT', 'ROLE_ADMIN')")
public class StudentController {

    private final StudentService studentService;
    private final AttendanceService attendanceService;
    private final HomeworkService homeworkService;
    private final ExamResultService examResultService;
    private final FeeService feeService;
    private final TimetableService timetableService;
    private final NoticeNotificationService noticeService;
    private final ExamRepository examRepository;

    public StudentController(StudentService studentService,
                             AttendanceService attendanceService,
                             HomeworkService homeworkService,
                             ExamResultService examResultService,
                             FeeService feeService,
                             TimetableService timetableService,
                             NoticeNotificationService noticeService,
                             ExamRepository examRepository) {
        this.studentService = studentService;
        this.attendanceService = attendanceService;
        this.homeworkService = homeworkService;
        this.examResultService = examResultService;
        this.feeService = feeService;
        this.timetableService = timetableService;
        this.noticeService = noticeService;
        this.examRepository = examRepository;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<StudentDashboardDTO>> getDashboard(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success("Student dashboard retrieved", studentService.getDashboard(principal.getId())));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<StudentResponseDTO>> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success("Student profile retrieved", studentService.getProfile(principal.getId())));
    }

    @GetMapping("/attendance")
    public ResponseEntity<ApiResponse<AttendanceSummaryDTO>> getAttendance(@AuthenticationPrincipal UserPrincipal principal) {
        Student student = studentService.getStudentByUserId(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Attendance summary retrieved", attendanceService.getStudentAttendanceSummary(student.getId())));
    }

    @GetMapping("/homework")
    public ResponseEntity<ApiResponse<List<HomeworkResponseDTO>>> getHomework(@AuthenticationPrincipal UserPrincipal principal) {
        Student student = studentService.getStudentByUserId(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Homework retrieved", homeworkService.getHomeworkForStudent(student.getId())));
    }

    @PostMapping("/homework/{id}/submit")
    public ResponseEntity<ApiResponse<HomeworkResponseDTO>> submitHomework(@PathVariable Long id,
                                                                           @Valid @RequestBody HomeworkSubmissionRequest request,
                                                                           @AuthenticationPrincipal UserPrincipal principal,
                                                                           HttpServletRequest httpRequest) {
        HomeworkResponseDTO response = homeworkService.submitHomework(principal.getId(), id, request, httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success("Homework submitted successfully", response));
    }

    @GetMapping("/timetable")
    public ResponseEntity<ApiResponse<List<TimetableEntryDTO>>> getTimetable(@AuthenticationPrincipal UserPrincipal principal) {
        Student student = studentService.getStudentByUserId(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Timetable retrieved", timetableService.getTimetableForStudent(student.getId())));
    }

    @GetMapping("/exams")
    public ResponseEntity<ApiResponse<List<Exam>>> getPublishedExams() {
        return ResponseEntity.ok(ApiResponse.success("Published exams retrieved", examRepository.findByIsPublishedTrueOrderByStartDateDesc()));
    }

    @GetMapping("/results/{examId}")
    public ResponseEntity<ApiResponse<StudentResultCardDTO>> getResultCard(@PathVariable Long examId,
                                                                           @AuthenticationPrincipal UserPrincipal principal) {
        Student student = studentService.getStudentByUserId(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Result card retrieved", examResultService.getStudentResultCard(student.getId(), examId)));
    }

    @GetMapping("/fees")
    public ResponseEntity<ApiResponse<List<StudentFeeDTO>>> getFees(@AuthenticationPrincipal UserPrincipal principal) {
        Student student = studentService.getStudentByUserId(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Fee dues retrieved", feeService.getStudentFees(student.getId())));
    }

    @GetMapping("/notices")
    public ResponseEntity<ApiResponse<List<NoticeResponseDTO>>> getNotices(@AuthenticationPrincipal UserPrincipal principal) {
        Student student = studentService.getStudentByUserId(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Notices retrieved", noticeService.getNoticesForAudienceAndSection(NoticeAudience.STUDENTS, student.getSection().getId())));
    }

    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getNotifications(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved", noticeService.getUserNotifications(principal.getId())));
    }

    @PostMapping("/notifications/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        noticeService.markNotificationAsRead(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read"));
    }
}
