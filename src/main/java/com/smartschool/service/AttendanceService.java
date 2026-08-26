package com.smartschool.service;

import com.smartschool.dto.AttendanceBatchRequest;
import com.smartschool.dto.AttendanceRecordDTO;
import com.smartschool.dto.AttendanceSummaryDTO;
import com.smartschool.entity.Attendance;
import com.smartschool.entity.Section;
import com.smartschool.entity.Student;
import com.smartschool.entity.Teacher;
import com.smartschool.entity.enums.AttendanceStatus;
import com.smartschool.exception.ResourceNotFoundException;
import com.smartschool.exception.UnauthorizedAccessException;
import com.smartschool.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final SectionRepository sectionRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final AuditService auditService;
    private final NoticeNotificationService notificationService;

    public AttendanceService(AttendanceRepository attendanceRepository,
                             SectionRepository sectionRepository,
                             StudentRepository studentRepository,
                             TeacherRepository teacherRepository,
                             TeacherAssignmentRepository teacherAssignmentRepository,
                             AuditService auditService,
                             NoticeNotificationService notificationService) {
        this.attendanceRepository = attendanceRepository;
        this.sectionRepository = sectionRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.teacherAssignmentRepository = teacherAssignmentRepository;
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    @Transactional
    public List<AttendanceRecordDTO> markBatchAttendance(Long teacherUserId, AttendanceBatchRequest request, String ipAddress) {
        Teacher teacher = teacherRepository.findByUserId(teacherUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher profile not found"));

        if (!teacherAssignmentRepository.existsByTeacherIdAndSectionId(teacher.getId(), request.getSectionId())) {
            throw new UnauthorizedAccessException("You are not authorized to mark attendance for this section");
        }

        Section section = sectionRepository.findById(request.getSectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Section not found"));

        List<AttendanceRecordDTO> results = new ArrayList<>();

        for (AttendanceBatchRequest.StudentAttendanceItem item : request.getItems()) {
            Student student = studentRepository.findById(item.getStudentId()).orElse(null);
            if (student != null) {
                Attendance attendance = attendanceRepository.findByStudentIdAndDate(student.getId(), request.getDate())
                        .orElse(new Attendance(student, section, request.getDate(), item.getStatus(), item.getRemarks(), teacher));

                attendance.setStatus(item.getStatus());
                attendance.setRemarks(item.getRemarks());
                attendance.setMarkedByTeacher(teacher);

                attendance = attendanceRepository.save(attendance);
                results.add(convertToDTO(attendance));

                // Check for attendance warning (< 75%)
                checkAndSendAttendanceAlert(student);
            }
        }

        auditService.log(teacher.getUser().getId(), teacher.getUser().getUsername(), "MARK_ATTENDANCE",
                "Section", section.getId(), "Marked attendance for " + section.getFullName() + " on " + request.getDate(), ipAddress);

        return results;
    }

    @Transactional(readOnly = true)
    public List<AttendanceRecordDTO> getAttendanceForSectionOnDate(Long sectionId, LocalDate date) {
        return attendanceRepository.findBySectionIdAndDate(sectionId, date).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AttendanceSummaryDTO getStudentAttendanceSummary(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        long totalDays = attendanceRepository.countByStudentId(studentId);
        long present = attendanceRepository.countByStudentIdAndStatus(studentId, AttendanceStatus.PRESENT);
        long absent = attendanceRepository.countByStudentIdAndStatus(studentId, AttendanceStatus.ABSENT);
        long late = attendanceRepository.countByStudentIdAndStatus(studentId, AttendanceStatus.LATE);
        long excused = attendanceRepository.countByStudentIdAndStatus(studentId, AttendanceStatus.EXCUSED);

        double percentage = totalDays > 0 ? (present * 100.0 / totalDays) : 100.0;
        percentage = Math.round(percentage * 10.0) / 10.0;

        AttendanceSummaryDTO summary = new AttendanceSummaryDTO();
        summary.setStudentId(student.getId());
        summary.setStudentName(student.getName());
        summary.setTotalWorkingDays(totalDays);
        summary.setPresentDays(present);
        summary.setAbsentDays(absent);
        summary.setLateDays(late);
        summary.setExcusedDays(excused);
        summary.setPercentage(percentage);
        summary.setLowAttendanceWarning(totalDays >= 5 && percentage < 75.0);

        List<AttendanceRecordDTO> recent = attendanceRepository.findByStudentIdOrderByDateDesc(studentId).stream()
                .limit(30)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        summary.setRecentRecords(recent);

        return summary;
    }

    private void checkAndSendAttendanceAlert(Student student) {
        long totalDays = attendanceRepository.countByStudentId(student.getId());
        if (totalDays >= 5) {
            long present = attendanceRepository.countByStudentIdAndStatus(student.getId(), AttendanceStatus.PRESENT);
            double percentage = (present * 100.0 / totalDays);
            if (percentage < 75.0) {
                notificationService.createNotification(
                        student.getUser().getId(),
                        "Attendance Warning",
                        String.format("Your current attendance is %.1f%%, which is below the mandatory 75%% threshold. Please consult your class teacher.", percentage),
                        "ATTENDANCE_ALERT",
                        "/student/attendance"
                );
            }
        }
    }

    public AttendanceRecordDTO convertToDTO(Attendance attendance) {
        AttendanceRecordDTO dto = new AttendanceRecordDTO();
        dto.setId(attendance.getId());
        dto.setStudentId(attendance.getStudent().getId());
        dto.setStudentName(attendance.getStudent().getName());
        dto.setRollNumber(attendance.getStudent().getRollNumber());
        dto.setAdmissionNumber(attendance.getStudent().getAdmissionNumber());
        dto.setSectionId(attendance.getSection().getId());
        dto.setDate(attendance.getDate());
        dto.setStatus(attendance.getStatus());
        dto.setRemarks(attendance.getRemarks());
        if (attendance.getMarkedByTeacher() != null) {
            dto.setMarkedByName(attendance.getMarkedByTeacher().getName());
        }
        return dto;
    }
}
