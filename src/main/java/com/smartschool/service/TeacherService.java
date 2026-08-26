package com.smartschool.service;

import com.smartschool.dto.*;
import com.smartschool.entity.*;
import com.smartschool.entity.enums.HomeworkStatus;
import com.smartschool.entity.enums.NoticeAudience;
import com.smartschool.exception.ResourceNotFoundException;
import com.smartschool.exception.UnauthorizedAccessException;
import com.smartschool.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final StudentRepository studentRepository;
    private final SectionRepository sectionRepository;
    private final HomeworkRepository homeworkRepository;
    private final HomeworkSubmissionRepository submissionRepository;
    private final ExamRepository examRepository;
    private final NoticeRepository noticeRepository;
    private final AdminService adminService;

    public TeacherService(TeacherRepository teacherRepository,
                          TeacherAssignmentRepository teacherAssignmentRepository,
                          StudentRepository studentRepository,
                          SectionRepository sectionRepository,
                          HomeworkRepository homeworkRepository,
                          HomeworkSubmissionRepository submissionRepository,
                          ExamRepository examRepository,
                          NoticeRepository noticeRepository,
                          AdminService adminService) {
        this.teacherRepository = teacherRepository;
        this.teacherAssignmentRepository = teacherAssignmentRepository;
        this.studentRepository = studentRepository;
        this.sectionRepository = sectionRepository;
        this.homeworkRepository = homeworkRepository;
        this.submissionRepository = submissionRepository;
        this.examRepository = examRepository;
        this.noticeRepository = noticeRepository;
        this.adminService = adminService;
    }

    @Transactional(readOnly = true)
    public Teacher getTeacherByUserId(Long userId) {
        return teacherRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher profile not found for user ID: " + userId));
    }

    @Transactional(readOnly = true)
    public TeacherDashboardDTO getDashboard(Long teacherUserId) {
        Teacher teacher = getTeacherByUserId(teacherUserId);

        TeacherDashboardDTO dto = new TeacherDashboardDTO();
        dto.setTeacherId(teacher.getId());
        dto.setTeacherName(teacher.getName());
        dto.setEmployeeId(teacher.getEmployeeId());

        List<TeacherAssignment> assignments = teacherAssignmentRepository.findByTeacherId(teacher.getId());
        List<SectionResponseDTO> sections = assignments.stream()
                .map(a -> adminService.convertSectionToDTO(a.getSection()))
                .distinct()
                .collect(Collectors.toList());
        dto.setAssignedClasses(sections);

        long totalStudents = sections.stream().mapToLong(s -> s.getStudentCount() != null ? s.getStudentCount() : 0).sum();
        dto.setTotalStudents(totalStudents);

        List<Homework> homeworkList = homeworkRepository.findByTeacherIdOrderByDueDateDesc(teacher.getId());
        long pendingReviews = homeworkList.stream()
                .mapToLong(hw -> submissionRepository.findByHomeworkId(hw.getId()).stream()
                        .filter(s -> s.getStatus() == HomeworkStatus.SUBMITTED).count())
                .sum();
        dto.setPendingHomeworkReviews(pendingReviews);

        dto.setUpcomingExamsCount(examRepository.count());

        List<NoticeResponseDTO> notices = noticeRepository.findNoticesForAudience(NoticeAudience.TEACHERS).stream()
                .limit(5)
                .map(n -> {
                    NoticeResponseDTO nDto = new NoticeResponseDTO();
                    nDto.setId(n.getId());
                    nDto.setTitle(n.getTitle());
                    nDto.setContent(n.getContent());
                    nDto.setAudience(n.getAudience());
                    nDto.setPublishedDate(n.getPublishedDate());
                    return nDto;
                })
                .collect(Collectors.toList());
        dto.setNotices(notices);

        return dto;
    }

    @Transactional(readOnly = true)
    public List<SectionResponseDTO> getMyClasses(Long teacherUserId) {
        Teacher teacher = getTeacherByUserId(teacherUserId);
        List<TeacherAssignment> assignments = teacherAssignmentRepository.findByTeacherId(teacher.getId());
        return assignments.stream()
                .map(a -> adminService.convertSectionToDTO(a.getSection()))
                .distinct()
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StudentResponseDTO> getStudentsInClass(Long teacherUserId, Long sectionId) {
        Teacher teacher = getTeacherByUserId(teacherUserId);
        if (!teacherAssignmentRepository.existsByTeacherIdAndSectionId(teacher.getId(), sectionId)) {
            throw new UnauthorizedAccessException("You are not authorized to view students in this class/section");
        }

        return studentRepository.findBySectionId(sectionId).stream()
                .map(adminService::convertStudentToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StudentResponseDTO getStudentById(Long teacherUserId, Long studentId) {
        Teacher teacher = getTeacherByUserId(teacherUserId);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        if (!teacherAssignmentRepository.existsByTeacherIdAndSectionId(teacher.getId(), student.getSection().getId())) {
            throw new UnauthorizedAccessException("You are not authorized to view this student");
        }

        return adminService.convertStudentToDTO(student);
    }

    @Transactional
    public StudentResponseDTO createStudentInAssignedClass(Long teacherUserId, StudentCreateRequest request, String ipAddress) {
        Teacher teacher = getTeacherByUserId(teacherUserId);
        if (!teacherAssignmentRepository.existsByTeacherIdAndSectionId(teacher.getId(), request.getSectionId())) {
            throw new UnauthorizedAccessException("You can only add students to classes assigned to you");
        }

        return adminService.createStudent(request, teacher.getUser().getId(), ipAddress);
    }

    @Transactional
    public StudentResponseDTO updateStudent(Long teacherUserId, Long studentId, StudentUpdateRequest request, String ipAddress) {
        Teacher teacher = getTeacherByUserId(teacherUserId);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        if (!teacherAssignmentRepository.existsByTeacherIdAndSectionId(teacher.getId(), student.getSection().getId())) {
            throw new UnauthorizedAccessException("You are not authorized to modify this student");
        }

        return adminService.updateStudent(studentId, request, teacher.getUser().getId(), ipAddress);
    }

    @Transactional
    public String resetStudentPassword(Long teacherUserId, Long studentId, String ipAddress) {
        Teacher teacher = getTeacherByUserId(teacherUserId);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        if (!teacherAssignmentRepository.existsByTeacherIdAndSectionId(teacher.getId(), student.getSection().getId())) {
            throw new UnauthorizedAccessException("You are not authorized to reset password for this student");
        }

        return adminService.resetUserPassword(student.getUser().getId(), teacher.getUser().getId(), ipAddress);
    }
}
