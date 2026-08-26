package com.smartschool.controller;

import com.smartschool.dto.*;
import com.smartschool.entity.HomeworkSubmission;
import com.smartschool.security.UserPrincipal;
import com.smartschool.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher")
@PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_ADMIN')")
public class TeacherController {

    private final TeacherService teacherService;
    private final AttendanceService attendanceService;
    private final HomeworkService homeworkService;
    private final ExamResultService examResultService;
    private final TimetableService timetableService;

    public TeacherController(TeacherService teacherService,
                             AttendanceService attendanceService,
                             HomeworkService homeworkService,
                             ExamResultService examResultService,
                             TimetableService timetableService) {
        this.teacherService = teacherService;
        this.attendanceService = attendanceService;
        this.homeworkService = homeworkService;
        this.examResultService = examResultService;
        this.timetableService = timetableService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<TeacherDashboardDTO>> getDashboard(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success("Teacher dashboard retrieved", teacherService.getDashboard(principal.getId())));
    }

    @GetMapping("/classes")
    public ResponseEntity<ApiResponse<List<SectionResponseDTO>>> getMyClasses(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success("Assigned classes retrieved", teacherService.getMyClasses(principal.getId())));
    }

    @GetMapping("/classes/{id}/students")
    public ResponseEntity<ApiResponse<List<StudentResponseDTO>>> getStudentsInClass(@PathVariable Long id,
                                                                                    @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success("Students retrieved", teacherService.getStudentsInClass(principal.getId(), id)));
    }

    @GetMapping("/students/{id}")
    public ResponseEntity<ApiResponse<StudentResponseDTO>> getStudentById(@PathVariable Long id,
                                                                          @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success("Student profile retrieved", teacherService.getStudentById(principal.getId(), id)));
    }

    @PostMapping("/students")
    public ResponseEntity<ApiResponse<StudentResponseDTO>> createStudent(@Valid @RequestBody StudentCreateRequest request,
                                                                         @AuthenticationPrincipal UserPrincipal principal,
                                                                         HttpServletRequest httpRequest) {
        StudentResponseDTO response = teacherService.createStudentInAssignedClass(principal.getId(), request, httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success("Student created successfully", response));
    }

    @PutMapping("/students/{id}")
    public ResponseEntity<ApiResponse<StudentResponseDTO>> updateStudent(@PathVariable Long id,
                                                                         @RequestBody StudentUpdateRequest request,
                                                                         @AuthenticationPrincipal UserPrincipal principal,
                                                                         HttpServletRequest httpRequest) {
        StudentResponseDTO response = teacherService.updateStudent(principal.getId(), id, request, httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success("Student updated successfully", response));
    }

    @PostMapping("/students/{id}/reset-password")
    public ResponseEntity<ApiResponse<Map<String, String>>> resetStudentPassword(@PathVariable Long id,
                                                                                 @AuthenticationPrincipal UserPrincipal principal,
                                                                                 HttpServletRequest httpRequest) {
        String tempPass = teacherService.resetStudentPassword(principal.getId(), id, httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success("Student password reset successfully", Map.of("temporaryPassword", tempPass)));
    }

    // ==================== ATTENDANCE ====================
    @PostMapping("/attendance")
    public ResponseEntity<ApiResponse<List<AttendanceRecordDTO>>> markAttendance(@Valid @RequestBody AttendanceBatchRequest request,
                                                                                 @AuthenticationPrincipal UserPrincipal principal,
                                                                                 HttpServletRequest httpRequest) {
        List<AttendanceRecordDTO> records = attendanceService.markBatchAttendance(principal.getId(), request, httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success("Attendance recorded successfully", records));
    }

    @GetMapping("/attendance/section/{id}")
    public ResponseEntity<ApiResponse<List<AttendanceRecordDTO>>> getAttendance(@PathVariable Long id,
                                                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        return ResponseEntity.ok(ApiResponse.success("Attendance retrieved", attendanceService.getAttendanceForSectionOnDate(id, targetDate)));
    }

    // ==================== HOMEWORK ====================
    @GetMapping("/homework")
    public ResponseEntity<ApiResponse<List<HomeworkResponseDTO>>> getHomework(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success("Homework retrieved", homeworkService.getHomeworkForTeacher(principal.getId())));
    }

    @PostMapping("/homework")
    public ResponseEntity<ApiResponse<HomeworkResponseDTO>> createHomework(@Valid @RequestBody HomeworkCreateRequest request,
                                                                           @AuthenticationPrincipal UserPrincipal principal,
                                                                           HttpServletRequest httpRequest) {
        HomeworkResponseDTO response = homeworkService.createHomework(principal.getId(), request, httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success("Homework created successfully", response));
    }

    @GetMapping("/homework/{id}/submissions")
    public ResponseEntity<ApiResponse<List<HomeworkSubmission>>> getSubmissions(@PathVariable Long id,
                                                                                @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success("Submissions retrieved", homeworkService.getSubmissionsForHomework(principal.getId(), id)));
    }

    @PostMapping("/homework/submissions/{id}/grade")
    public ResponseEntity<ApiResponse<Void>> gradeSubmission(@PathVariable Long id,
                                                             @Valid @RequestBody HomeworkGradeRequest request,
                                                             @AuthenticationPrincipal UserPrincipal principal,
                                                             HttpServletRequest httpRequest) {
        homeworkService.gradeHomework(principal.getId(), id, request, httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success("Submission graded successfully"));
    }

    // ==================== MARKS ====================
    @PostMapping("/marks")
    public ResponseEntity<ApiResponse<MarkEntryDTO>> enterMarks(@Valid @RequestBody MarkEntryDTO dto,
                                                                @AuthenticationPrincipal UserPrincipal principal,
                                                                HttpServletRequest httpRequest) {
        MarkEntryDTO result = examResultService.enterMarks(principal.getId(), dto, httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success("Marks saved successfully", result));
    }

    // ==================== TIMETABLE ====================
    @GetMapping("/timetable")
    public ResponseEntity<ApiResponse<List<TimetableEntryDTO>>> getMyTimetable(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success("Teacher timetable retrieved", timetableService.getTimetableForTeacher(principal.getId())));
    }
}
