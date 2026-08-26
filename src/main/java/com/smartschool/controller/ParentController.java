package com.smartschool.controller;

import com.smartschool.dto.*;
import com.smartschool.entity.enums.NoticeAudience;
import com.smartschool.entity.Student;
import com.smartschool.repository.StudentRepository;
import com.smartschool.security.UserPrincipal;
import com.smartschool.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parent")
@PreAuthorize("hasAnyAuthority('ROLE_PARENT', 'ROLE_ADMIN')")
public class ParentController {

    private final ParentService parentService;
    private final AttendanceService attendanceService;
    private final HomeworkService homeworkService;
    private final ExamResultService examResultService;
    private final FeeService feeService;
    private final TimetableService timetableService;
    private final NoticeNotificationService noticeService;
    private final StudentRepository studentRepository;

    public ParentController(ParentService parentService,
                            AttendanceService attendanceService,
                            HomeworkService homeworkService,
                            ExamResultService examResultService,
                            FeeService feeService,
                            TimetableService timetableService,
                            NoticeNotificationService noticeService,
                            StudentRepository studentRepository) {
        this.parentService = parentService;
        this.attendanceService = attendanceService;
        this.homeworkService = homeworkService;
        this.examResultService = examResultService;
        this.feeService = feeService;
        this.timetableService = timetableService;
        this.noticeService = noticeService;
        this.studentRepository = studentRepository;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<ParentDashboardDTO>> getDashboard(@RequestParam(required = false) Long childId,
                                                                        @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success("Parent dashboard retrieved", parentService.getDashboard(principal.getId(), childId)));
    }

    @GetMapping("/children")
    public ResponseEntity<ApiResponse<List<StudentResponseDTO>>> getChildren(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success("Children list retrieved", parentService.getChildren(principal.getId())));
    }

    @GetMapping("/children/{id}/attendance")
    public ResponseEntity<ApiResponse<AttendanceSummaryDTO>> getChildAttendance(@PathVariable Long id,
                                                                                @AuthenticationPrincipal UserPrincipal principal) {
        parentService.verifyChildOwnership(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Child attendance retrieved", attendanceService.getStudentAttendanceSummary(id)));
    }

    @GetMapping("/children/{id}/homework")
    public ResponseEntity<ApiResponse<List<HomeworkResponseDTO>>> getChildHomework(@PathVariable Long id,
                                                                                   @AuthenticationPrincipal UserPrincipal principal) {
        parentService.verifyChildOwnership(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Child homework retrieved", homeworkService.getHomeworkForStudent(id)));
    }

    @GetMapping("/children/{id}/results/{examId}")
    public ResponseEntity<ApiResponse<StudentResultCardDTO>> getChildResults(@PathVariable Long id,
                                                                             @PathVariable Long examId,
                                                                             @AuthenticationPrincipal UserPrincipal principal) {
        parentService.verifyChildOwnership(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Child result card retrieved", examResultService.getStudentResultCard(id, examId)));
    }

    @GetMapping("/children/{id}/fees")
    public ResponseEntity<ApiResponse<List<StudentFeeDTO>>> getChildFees(@PathVariable Long id,
                                                                         @AuthenticationPrincipal UserPrincipal principal) {
        parentService.verifyChildOwnership(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Child fee details retrieved", feeService.getStudentFees(id)));
    }

    @GetMapping("/children/{id}/timetable")
    public ResponseEntity<ApiResponse<List<TimetableEntryDTO>>> getChildTimetable(@PathVariable Long id,
                                                                                  @AuthenticationPrincipal UserPrincipal principal) {
        parentService.verifyChildOwnership(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Child timetable retrieved", timetableService.getTimetableForStudent(id)));
    }

    @GetMapping("/notices")
    public ResponseEntity<ApiResponse<List<NoticeResponseDTO>>> getParentNotices() {
        return ResponseEntity.ok(ApiResponse.success("Notices retrieved", noticeService.getNoticesForAudienceAndSection(NoticeAudience.PARENTS, null)));
    }
}
