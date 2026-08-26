package com.smartschool.service;

import com.smartschool.dto.HomeworkCreateRequest;
import com.smartschool.dto.HomeworkGradeRequest;
import com.smartschool.dto.HomeworkResponseDTO;
import com.smartschool.dto.HomeworkSubmissionRequest;
import com.smartschool.entity.*;
import com.smartschool.entity.enums.HomeworkStatus;
import com.smartschool.exception.BadRequestException;
import com.smartschool.exception.ResourceNotFoundException;
import com.smartschool.exception.UnauthorizedAccessException;
import com.smartschool.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class HomeworkService {

    private final HomeworkRepository homeworkRepository;
    private final HomeworkSubmissionRepository submissionRepository;
    private final TeacherRepository teacherRepository;
    private final SectionRepository sectionRepository;
    private final SubjectRepository subjectRepository;
    private final StudentRepository studentRepository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final AuditService auditService;
    private final NoticeNotificationService notificationService;

    public HomeworkService(HomeworkRepository homeworkRepository,
                           HomeworkSubmissionRepository submissionRepository,
                           TeacherRepository teacherRepository,
                           SectionRepository sectionRepository,
                           SubjectRepository subjectRepository,
                           StudentRepository studentRepository,
                           TeacherAssignmentRepository teacherAssignmentRepository,
                           AuditService auditService,
                           NoticeNotificationService notificationService) {
        this.homeworkRepository = homeworkRepository;
        this.submissionRepository = submissionRepository;
        this.teacherRepository = teacherRepository;
        this.sectionRepository = sectionRepository;
        this.subjectRepository = subjectRepository;
        this.studentRepository = studentRepository;
        this.teacherAssignmentRepository = teacherAssignmentRepository;
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    @Transactional
    public HomeworkResponseDTO createHomework(Long teacherUserId, HomeworkCreateRequest request, String ipAddress) {
        Teacher teacher = teacherRepository.findByUserId(teacherUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));

        if (!teacherAssignmentRepository.existsByTeacherIdAndSectionId(teacher.getId(), request.getSectionId())) {
            throw new UnauthorizedAccessException("You are not assigned to this class section");
        }

        Section section = sectionRepository.findById(request.getSectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Section not found"));

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));

        Homework homework = new Homework();
        homework.setSection(section);
        homework.setSubject(subject);
        homework.setTeacher(teacher);
        homework.setTitle(request.getTitle());
        homework.setDescription(request.getDescription());
        homework.setDueDate(request.getDueDate());
        homework.setAttachmentUrl(request.getAttachmentUrl());

        homework = homeworkRepository.save(homework);

        // Notify all students in this section
        List<Student> students = studentRepository.findBySectionId(section.getId());
        for (Student s : students) {
            notificationService.createNotification(
                    s.getUser().getId(),
                    "New Homework: " + homework.getTitle(),
                    "Homework assigned for " + subject.getSubjectName() + ". Due on " + homework.getDueDate(),
                    "HOMEWORK",
                    "/student/homework"
            );
        }

        auditService.log(teacher.getUser().getId(), teacher.getUser().getUsername(), "CREATE_HOMEWORK",
                "Homework", homework.getId(), "Created homework: " + homework.getTitle() + " for " + section.getFullName(), ipAddress);

        return convertToDTO(homework, null);
    }

    @Transactional(readOnly = true)
    public List<HomeworkResponseDTO> getHomeworkForTeacher(Long teacherUserId) {
        Teacher teacher = teacherRepository.findByUserId(teacherUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));

        return homeworkRepository.findByTeacherIdOrderByDueDateDesc(teacher.getId()).stream()
                .map(hw -> convertToDTO(hw, null))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<HomeworkResponseDTO> getHomeworkForStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        List<Homework> homeworkList = homeworkRepository.findBySectionIdOrderByDueDateDesc(student.getSection().getId());
        return homeworkList.stream()
                .map(hw -> {
                    HomeworkSubmission sub = submissionRepository.findByHomeworkIdAndStudentId(hw.getId(), student.getId()).orElse(null);
                    return convertToDTO(hw, sub);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public HomeworkResponseDTO submitHomework(Long studentUserId, Long homeworkId, HomeworkSubmissionRequest request, String ipAddress) {
        Student student = studentRepository.findByUserId(studentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        Homework homework = homeworkRepository.findById(homeworkId)
                .orElseThrow(() -> new ResourceNotFoundException("Homework not found"));

        if (!homework.getSection().getId().equals(student.getSection().getId())) {
            throw new UnauthorizedAccessException("This homework was not assigned to your section");
        }

        HomeworkSubmission submission = submissionRepository.findByHomeworkIdAndStudentId(homework.getId(), student.getId())
                .orElse(new HomeworkSubmission());

        submission.setHomework(homework);
        submission.setStudent(student);
        submission.setContent(request.getContent());
        submission.setAttachmentUrl(request.getAttachmentUrl());
        submission.setSubmissionDate(LocalDateTime.now());

        boolean isLate = LocalDate.now().isAfter(homework.getDueDate());
        submission.setStatus(isLate ? HomeworkStatus.LATE : HomeworkStatus.SUBMITTED);

        submission = submissionRepository.save(submission);

        auditService.log(student.getUser().getId(), student.getUser().getUsername(), "SUBMIT_HOMEWORK",
                "HomeworkSubmission", submission.getId(), "Submitted homework for " + homework.getTitle(), ipAddress);

        return convertToDTO(homework, submission);
    }

    @Transactional
    public void gradeHomework(Long teacherUserId, Long submissionId, HomeworkGradeRequest request, String ipAddress) {
        Teacher teacher = teacherRepository.findByUserId(teacherUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));

        HomeworkSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));

        if (!submission.getHomework().getTeacher().getId().equals(teacher.getId())) {
            throw new UnauthorizedAccessException("You are not the creator of this homework");
        }

        submission.setStatus(request.getStatus());
        submission.setMarks(request.getMarks());
        submission.setFeedback(request.getFeedback());

        submissionRepository.save(submission);

        notificationService.createNotification(
                submission.getStudent().getUser().getId(),
                "Homework Graded: " + submission.getHomework().getTitle(),
                "Your homework has been reviewed. Marks: " + request.getMarks() + ", Feedback: " + request.getFeedback(),
                "HOMEWORK_GRADED",
                "/student/homework"
        );

        auditService.log(teacher.getUser().getId(), teacher.getUser().getUsername(), "GRADE_HOMEWORK",
                "HomeworkSubmission", submission.getId(), "Graded submission for student " + submission.getStudent().getName(), ipAddress);
    }

    @Transactional(readOnly = true)
    public List<HomeworkSubmission> getSubmissionsForHomework(Long teacherUserId, Long homeworkId) {
        Teacher teacher = teacherRepository.findByUserId(teacherUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));

        Homework homework = homeworkRepository.findById(homeworkId)
                .orElseThrow(() -> new ResourceNotFoundException("Homework not found"));

        if (!homework.getTeacher().getId().equals(teacher.getId())) {
            throw new UnauthorizedAccessException("You cannot view submissions for other teachers' homework");
        }

        return submissionRepository.findByHomeworkId(homeworkId);
    }

    public HomeworkResponseDTO convertToDTO(Homework homework, HomeworkSubmission submission) {
        HomeworkResponseDTO dto = new HomeworkResponseDTO();
        dto.setId(homework.getId());
        dto.setSectionId(homework.getSection().getId());
        dto.setSectionFullName(homework.getSection().getFullName());
        dto.setSubjectId(homework.getSubject().getId());
        dto.setSubjectName(homework.getSubject().getSubjectName());
        dto.setTeacherId(homework.getTeacher().getId());
        dto.setTeacherName(homework.getTeacher().getName());
        dto.setTitle(homework.getTitle());
        dto.setDescription(homework.getDescription());
        dto.setAssignedDate(homework.getAssignedDate());
        dto.setDueDate(homework.getDueDate());
        dto.setAttachmentUrl(homework.getAttachmentUrl());

        if (submission != null) {
            dto.setSubmissionStatus(submission.getStatus());
            dto.setSubmittedAt(submission.getSubmissionDate());
            dto.setStudentContent(submission.getContent());
            dto.setStudentAttachmentUrl(submission.getAttachmentUrl());
            dto.setFeedback(submission.getFeedback());
            dto.setMarks(submission.getMarks());
        } else {
            dto.setSubmissionStatus(HomeworkStatus.PENDING);
        }

        return dto;
    }
}
