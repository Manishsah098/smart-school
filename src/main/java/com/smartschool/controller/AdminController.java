package com.smartschool.controller;

import com.smartschool.dto.*;
import com.smartschool.entity.*;
import com.smartschool.repository.AcademicYearRepository;
import com.smartschool.repository.SubjectRepository;
import com.smartschool.security.UserPrincipal;
import com.smartschool.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final FeeService feeService;
    private final ExamResultService examResultService;
    private final TimetableService timetableService;
    private final NoticeNotificationService noticeService;
    private final ReportService reportService;
    private final AuditService auditService;
    private final SubjectRepository subjectRepository;
    private final AcademicYearRepository academicYearRepository;

    public AdminController(AdminService adminService,
                           FeeService feeService,
                           ExamResultService examResultService,
                           TimetableService timetableService,
                           NoticeNotificationService noticeService,
                           ReportService reportService,
                           AuditService auditService,
                           SubjectRepository subjectRepository,
                           AcademicYearRepository academicYearRepository) {
        this.adminService = adminService;
        this.feeService = feeService;
        this.examResultService = examResultService;
        this.timetableService = timetableService;
        this.noticeService = noticeService;
        this.reportService = reportService;
        this.auditService = auditService;
        this.subjectRepository = subjectRepository;
        this.academicYearRepository = academicYearRepository;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardDTO>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success("Admin dashboard stats retrieved", adminService.getDashboardStats()));
    }

    // ==================== TEACHERS ====================
    @GetMapping("/teachers")
    public ResponseEntity<ApiResponse<List<TeacherResponseDTO>>> getAllTeachers() {
        return ResponseEntity.ok(ApiResponse.success("Teachers retrieved", adminService.getAllTeachers()));
    }

    @PostMapping("/teachers")
    public ResponseEntity<ApiResponse<TeacherResponseDTO>> createTeacher(@Valid @RequestBody TeacherCreateRequest request,
                                                                         @AuthenticationPrincipal UserPrincipal principal,
                                                                         HttpServletRequest httpRequest) {
        TeacherResponseDTO response = adminService.createTeacher(request, principal.getId(), httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success("Teacher created successfully", response));
    }

    @PutMapping("/teachers/{id}")
    public ResponseEntity<ApiResponse<TeacherResponseDTO>> updateTeacher(@PathVariable Long id,
                                                                         @RequestBody TeacherCreateRequest request,
                                                                         @AuthenticationPrincipal UserPrincipal principal,
                                                                         HttpServletRequest httpRequest) {
        TeacherResponseDTO response = adminService.updateTeacher(id, request, principal.getId(), httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success("Teacher updated successfully", response));
    }

    // ==================== STUDENTS ====================
    @GetMapping("/students")
    public ResponseEntity<ApiResponse<List<StudentResponseDTO>>> getAllStudents() {
        return ResponseEntity.ok(ApiResponse.success("Students retrieved", adminService.getAllStudents()));
    }

    @PostMapping("/students")
    public ResponseEntity<ApiResponse<StudentResponseDTO>> createStudent(@Valid @RequestBody StudentCreateRequest request,
                                                                         @AuthenticationPrincipal UserPrincipal principal,
                                                                         HttpServletRequest httpRequest) {
        StudentResponseDTO response = adminService.createStudent(request, principal.getId(), httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success("Student created successfully", response));
    }

    @PutMapping("/students/{id}")
    public ResponseEntity<ApiResponse<StudentResponseDTO>> updateStudent(@PathVariable Long id,
                                                                         @RequestBody StudentUpdateRequest request,
                                                                         @AuthenticationPrincipal UserPrincipal principal,
                                                                         HttpServletRequest httpRequest) {
        StudentResponseDTO response = adminService.updateStudent(id, request, principal.getId(), httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success("Student updated successfully", response));
    }

    // ==================== PASSWORD RESET ====================
    @PostMapping("/users/{id}/reset-password")
    public ResponseEntity<ApiResponse<Map<String, String>>> resetPassword(@PathVariable Long id,
                                                                          @AuthenticationPrincipal UserPrincipal principal,
                                                                          HttpServletRequest httpRequest) {
        String tempPassword = adminService.resetUserPassword(id, principal.getId(), httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully", Map.of("temporaryPassword", tempPassword)));
    }

    // ==================== CLASSES & SECTIONS ====================
    @GetMapping("/classes")
    public ResponseEntity<ApiResponse<List<ClassResponseDTO>>> getAllClasses() {
        return ResponseEntity.ok(ApiResponse.success("Classes retrieved", adminService.getAllClasses()));
    }

    @PostMapping("/classes")
    public ResponseEntity<ApiResponse<SchoolClass>> createClass(@RequestParam String className,
                                                                @RequestParam(required = false, defaultValue = "1") Long academicYearId) {
        SchoolClass sc = adminService.createClass(className, academicYearId);
        return ResponseEntity.ok(ApiResponse.success("Class created successfully", sc));
    }

    @PostMapping("/sections")
    public ResponseEntity<ApiResponse<SectionResponseDTO>> createSection(@Valid @RequestBody SectionCreateRequest request) {
        SectionResponseDTO dto = adminService.createSection(request);
        return ResponseEntity.ok(ApiResponse.success("Section created successfully", dto));
    }

    // ==================== SUBJECTS ====================
    @GetMapping("/subjects")
    public ResponseEntity<ApiResponse<List<Subject>>> getAllSubjects() {
        return ResponseEntity.ok(ApiResponse.success("Subjects retrieved", subjectRepository.findAll()));
    }

    @PostMapping("/subjects")
    public ResponseEntity<ApiResponse<Subject>> createSubject(@Valid @RequestBody SubjectDTO dto) {
        Subject subject = new Subject(dto.getSubjectName(), dto.getSubjectCode());
        subject = subjectRepository.save(subject);
        return ResponseEntity.ok(ApiResponse.success("Subject created successfully", subject));
    }

    // ==================== ACADEMIC YEARS ====================
    @GetMapping("/academic-years")
    public ResponseEntity<ApiResponse<List<AcademicYear>>> getAllAcademicYears() {
        return ResponseEntity.ok(ApiResponse.success("Academic years retrieved", academicYearRepository.findAll()));
    }

    // ==================== FEES ====================
    @GetMapping("/fees")
    public ResponseEntity<ApiResponse<List<FeeResponseDTO>>> getAllFees() {
        return ResponseEntity.ok(ApiResponse.success("Fees retrieved", feeService.getAllFees()));
    }

    @PostMapping("/fees")
    public ResponseEntity<ApiResponse<FeeResponseDTO>> createFee(@Valid @RequestBody FeeCreateRequest request,
                                                                 @AuthenticationPrincipal UserPrincipal principal,
                                                                 HttpServletRequest httpRequest) {
        FeeResponseDTO response = feeService.createFee(request, principal.getId(), httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success("Fee structure created successfully", response));
    }

    @PostMapping("/fees/collect")
    public ResponseEntity<ApiResponse<StudentFeeDTO>> recordPayment(@Valid @RequestBody FeePaymentRequest request,
                                                                    @AuthenticationPrincipal UserPrincipal principal,
                                                                    HttpServletRequest httpRequest) {
        StudentFeeDTO response = feeService.recordPayment(request, principal.getId(), httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success("Fee payment recorded successfully", response));
    }

    // ==================== EXAMS ====================
    @PostMapping("/exams")
    public ResponseEntity<ApiResponse<Exam>> createExam(@Valid @RequestBody ExamCreateRequest request,
                                                        @AuthenticationPrincipal UserPrincipal principal,
                                                        HttpServletRequest httpRequest) {
        Exam exam = examResultService.createExam(request, principal.getId(), httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success("Exam created successfully", exam));
    }

    @PostMapping("/exams/{id}/publish")
    public ResponseEntity<ApiResponse<Void>> publishExam(@PathVariable Long id,
                                                         @RequestParam boolean isPublished,
                                                         @AuthenticationPrincipal UserPrincipal principal,
                                                         HttpServletRequest httpRequest) {
        examResultService.publishExam(id, isPublished, principal.getId(), httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success("Exam publish status updated successfully"));
    }

    @PostMapping("/exam-schedules")
    public ResponseEntity<ApiResponse<ExamScheduleDTO>> createExamSchedule(@Valid @RequestBody ExamScheduleDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("Exam schedule created", examResultService.createExamSchedule(dto)));
    }

    // ==================== TIMETABLE ====================
    @PostMapping("/timetables")
    public ResponseEntity<ApiResponse<TimetableEntryDTO>> createTimetableEntry(@Valid @RequestBody TimetableEntryDTO dto,
                                                                               @AuthenticationPrincipal UserPrincipal principal,
                                                                               HttpServletRequest httpRequest) {
        TimetableEntryDTO res = timetableService.createEntry(dto, principal.getId(), httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success("Timetable entry created", res));
    }

    @GetMapping("/timetables/section/{id}")
    public ResponseEntity<ApiResponse<List<TimetableEntryDTO>>> getSectionTimetable(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Section timetable retrieved", timetableService.getTimetableForSection(id)));
    }

    // ==================== NOTICES ====================
    @GetMapping("/notices")
    public ResponseEntity<ApiResponse<List<NoticeResponseDTO>>> getAllNotices() {
        return ResponseEntity.ok(ApiResponse.success("Notices retrieved", noticeService.getAllNotices()));
    }

    @PostMapping("/notices")
    public ResponseEntity<ApiResponse<NoticeResponseDTO>> createNotice(@Valid @RequestBody NoticeCreateRequest request,
                                                                       @AuthenticationPrincipal UserPrincipal principal,
                                                                       HttpServletRequest httpRequest) {
        NoticeResponseDTO res = noticeService.createNotice(request, principal.getId(), httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success("Notice published successfully", res));
    }

    // ==================== AUDIT & REPORTS ====================
    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<List<AuditLogDTO>>> getAuditLogs() {
        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved", auditService.getRecentLogs()));
    }

    @GetMapping("/reports/attendance")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAttendanceReport(@RequestParam Long sectionId) {
        return ResponseEntity.ok(ApiResponse.success("Attendance report generated", reportService.generateSectionAttendanceReport(sectionId)));
    }

    @GetMapping("/reports/exam")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getExamReport(@RequestParam Long examId, @RequestParam Long sectionId) {
        return ResponseEntity.ok(ApiResponse.success("Exam report generated", reportService.generateExamReport(examId, sectionId)));
    }

    @GetMapping("/reports/fees")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getFeeReport() {
        return ResponseEntity.ok(ApiResponse.success("Fee report generated", reportService.generateFeeCollectionReport()));
    }
}
