package com.smartschool.service;

import com.smartschool.dto.ExamCreateRequest;
import com.smartschool.dto.ExamScheduleDTO;
import com.smartschool.dto.MarkEntryDTO;
import com.smartschool.dto.StudentResultCardDTO;
import com.smartschool.entity.*;
import com.smartschool.exception.BadRequestException;
import com.smartschool.exception.ResourceNotFoundException;
import com.smartschool.exception.UnauthorizedAccessException;
import com.smartschool.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ExamResultService {

    private final ExamRepository examRepository;
    private final ExamScheduleRepository examScheduleRepository;
    private final MarkRepository markRepository;
    private final AcademicYearRepository academicYearRepository;
    private final SectionRepository sectionRepository;
    private final SubjectRepository subjectRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final AuditService auditService;
    private final NoticeNotificationService notificationService;

    public ExamResultService(ExamRepository examRepository,
                             ExamScheduleRepository examScheduleRepository,
                             MarkRepository markRepository,
                             AcademicYearRepository academicYearRepository,
                             SectionRepository sectionRepository,
                             SubjectRepository subjectRepository,
                             StudentRepository studentRepository,
                             TeacherRepository teacherRepository,
                             TeacherAssignmentRepository teacherAssignmentRepository,
                             AuditService auditService,
                             NoticeNotificationService notificationService) {
        this.examRepository = examRepository;
        this.examScheduleRepository = examScheduleRepository;
        this.markRepository = markRepository;
        this.academicYearRepository = academicYearRepository;
        this.sectionRepository = sectionRepository;
        this.subjectRepository = subjectRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.teacherAssignmentRepository = teacherAssignmentRepository;
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    @Transactional
    public Exam createExam(ExamCreateRequest request, Long adminUserId, String ipAddress) {
        AcademicYear academicYear = academicYearRepository.findById(request.getAcademicYearId())
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found"));

        Exam exam = new Exam();
        exam.setAcademicYear(academicYear);
        exam.setName(request.getName());
        exam.setExamType(request.getExamType());
        exam.setStartDate(request.getStartDate());
        exam.setEndDate(request.getEndDate());
        exam.setPublished(request.isPublished());

        exam = examRepository.save(exam);

        auditService.log(adminUserId, "admin", "CREATE_EXAM", "Exam", exam.getId(),
                "Created examination: " + exam.getName(), ipAddress);

        return exam;
    }

    @Transactional
    public void publishExam(Long examId, boolean isPublished, Long adminUserId, String ipAddress) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found"));

        exam.setPublished(isPublished);
        examRepository.save(exam);

        if (isPublished) {
            // Send notice / notification
            auditService.log(adminUserId, "admin", "PUBLISH_RESULTS", "Exam", exam.getId(),
                    "Published results for " + exam.getName(), ipAddress);
        }
    }

    @Transactional
    public ExamScheduleDTO createExamSchedule(ExamScheduleDTO dto) {
        Exam exam = examRepository.findById(dto.getExamId())
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found"));
        Section section = sectionRepository.findById(dto.getSectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Section not found"));
        Subject subject = subjectRepository.findById(dto.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));

        ExamSchedule schedule = new ExamSchedule();
        schedule.setExam(exam);
        schedule.setSection(section);
        schedule.setSubject(subject);
        schedule.setExamDate(dto.getExamDate());
        schedule.setStartTime(dto.getStartTime());
        schedule.setEndTime(dto.getEndTime());
        schedule.setRoomNumber(dto.getRoomNumber());
        schedule.setMaxMarks(dto.getMaxMarks() != null ? dto.getMaxMarks() : 100.0);
        schedule.setPassingMarks(dto.getPassingMarks() != null ? dto.getPassingMarks() : 35.0);

        schedule = examScheduleRepository.save(schedule);
        return convertScheduleToDTO(schedule);
    }

    @Transactional
    public MarkEntryDTO enterMarks(Long teacherUserId, MarkEntryDTO dto, String ipAddress) {
        Teacher teacher = teacherRepository.findByUserId(teacherUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));

        ExamSchedule schedule = examScheduleRepository.findById(dto.getExamScheduleId())
                .orElseThrow(() -> new ResourceNotFoundException("Exam schedule not found"));

        if (!teacherAssignmentRepository.existsByTeacherIdAndSectionId(teacher.getId(), schedule.getSection().getId())) {
            throw new UnauthorizedAccessException("You are not assigned to this section");
        }

        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        if (dto.getMarksObtained() > schedule.getMaxMarks() || dto.getMarksObtained() < 0) {
            throw new BadRequestException("Marks must be between 0 and " + schedule.getMaxMarks());
        }

        Mark mark = markRepository.findByExamScheduleIdAndStudentId(schedule.getId(), student.getId())
                .orElse(new Mark(schedule, student, dto.getMarksObtained(), null, dto.getRemarks(), teacher));

        mark.setMarksObtained(dto.getMarksObtained());
        mark.setRemarks(dto.getRemarks());
        mark.setGrade(calculateGrade(dto.getMarksObtained(), schedule.getMaxMarks()));
        mark.setEnteredByTeacher(teacher);

        mark = markRepository.save(mark);

        auditService.log(teacher.getUser().getId(), teacher.getUser().getUsername(), "ENTER_MARKS",
                "Mark", mark.getId(), "Entered marks for " + student.getName() + " (" + schedule.getSubject().getSubjectName() + "): " + dto.getMarksObtained(), ipAddress);

        dto.setId(mark.getId());
        dto.setGrade(mark.getGrade());
        dto.setMaxMarks(schedule.getMaxMarks());
        return dto;
    }

    @Transactional(readOnly = true)
    public StudentResultCardDTO getStudentResultCard(Long studentId, Long examId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found"));

        List<ExamSchedule> schedules = examScheduleRepository.findByExamIdAndSectionId(examId, student.getSection().getId());
        List<StudentResultCardDTO.SubjectResultItem> items = new ArrayList<>();

        double totalObtained = 0.0;
        double totalMax = 0.0;
        boolean hasFailedSubject = false;

        for (ExamSchedule sch : schedules) {
            Optional<Mark> markOpt = markRepository.findByExamScheduleIdAndStudentId(sch.getId(), student.getId());
            double obtained = markOpt.map(Mark::getMarksObtained).orElse(0.0);
            String grade = markOpt.map(Mark::getGrade).orElse("F");
            String remarks = markOpt.map(Mark::getRemarks).orElse(markOpt.isPresent() ? "" : "Absent/Pending");

            if (obtained < sch.getPassingMarks()) {
                hasFailedSubject = true;
            }

            totalObtained += obtained;
            totalMax += sch.getMaxMarks();

            items.add(new StudentResultCardDTO.SubjectResultItem(
                    sch.getSubject().getSubjectName(),
                    sch.getMaxMarks(),
                    sch.getPassingMarks(),
                    obtained,
                    grade,
                    remarks
            ));
        }

        double percentage = totalMax > 0 ? (totalObtained * 100.0 / totalMax) : 0.0;
        percentage = Math.round(percentage * 10.0) / 10.0;

        StudentResultCardDTO card = new StudentResultCardDTO();
        card.setExamId(exam.getId());
        card.setExamName(exam.getName());
        card.setStudentId(student.getId());
        card.setStudentName(student.getName());
        card.setAdmissionNumber(student.getAdmissionNumber());
        card.setClassSectionName(student.getSection().getFullName());
        card.setRollNumber(student.getRollNumber());
        card.setSubjects(items);
        card.setTotalMarksObtained(totalObtained);
        card.setTotalMaxMarks(totalMax);
        card.setPercentage(percentage);
        card.setOverallGrade(calculateGrade(totalObtained, totalMax));
        card.setResultStatus(hasFailedSubject ? "FAIL" : "PASS");

        return card;
    }

    @Transactional(readOnly = true)
    public double getStudentAverageMarks(Long studentId) {
        List<Mark> marks = markRepository.findByStudentId(studentId);
        if (marks.isEmpty()) return 0.0;

        double totalObtained = 0.0;
        double totalMax = 0.0;
        for (Mark m : marks) {
            totalObtained += m.getMarksObtained();
            totalMax += m.getExamSchedule().getMaxMarks();
        }
        return totalMax > 0 ? Math.round((totalObtained * 100.0 / totalMax) * 10.0) / 10.0 : 0.0;
    }

    public static String calculateGrade(double obtained, double max) {
        if (max <= 0) return "N/A";
        double percent = (obtained * 100.0 / max);
        if (percent >= 90.0) return "A+";
        if (percent >= 80.0) return "A";
        if (percent >= 70.0) return "B";
        if (percent >= 60.0) return "C";
        if (percent >= 50.0) return "D";
        if (percent >= 35.0) return "E";
        return "F";
    }

    public ExamScheduleDTO convertScheduleToDTO(ExamSchedule schedule) {
        ExamScheduleDTO dto = new ExamScheduleDTO();
        dto.setId(schedule.getId());
        dto.setExamId(schedule.getExam().getId());
        dto.setExamName(schedule.getExam().getName());
        dto.setSectionId(schedule.getSection().getId());
        dto.setSectionFullName(schedule.getSection().getFullName());
        dto.setSubjectId(schedule.getSubject().getId());
        dto.setSubjectName(schedule.getSubject().getSubjectName());
        dto.setExamDate(schedule.getExamDate());
        dto.setStartTime(schedule.getStartTime());
        dto.setEndTime(schedule.getEndTime());
        dto.setRoomNumber(schedule.getRoomNumber());
        dto.setMaxMarks(schedule.getMaxMarks());
        dto.setPassingMarks(schedule.getPassingMarks());
        return dto;
    }
}
