package com.smartschool.service;

import com.smartschool.dto.*;
import com.smartschool.entity.*;
import com.smartschool.entity.enums.*;
import com.smartschool.exception.BadRequestException;
import com.smartschool.exception.ResourceNotFoundException;
import com.smartschool.repository.*;
import com.smartschool.util.PasswordGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    private final ParentStudentRepository parentStudentRepository;
    private final SchoolClassRepository classRepository;
    private final SectionRepository sectionRepository;
    private final SubjectRepository subjectRepository;
    private final AcademicYearRepository academicYearRepository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final StudentFeeRepository studentFeeRepository;
    private final ExamRepository examRepository;
    private final NoticeRepository noticeRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public AdminService(UserRepository userRepository,
                        TeacherRepository teacherRepository,
                        StudentRepository studentRepository,
                        ParentRepository parentRepository,
                        ParentStudentRepository parentStudentRepository,
                        SchoolClassRepository classRepository,
                        SectionRepository sectionRepository,
                        SubjectRepository subjectRepository,
                        AcademicYearRepository academicYearRepository,
                        TeacherAssignmentRepository teacherAssignmentRepository,
                        AttendanceRepository attendanceRepository,
                        StudentFeeRepository studentFeeRepository,
                        ExamRepository examRepository,
                        NoticeRepository noticeRepository,
                        AuditLogRepository auditLogRepository,
                        PasswordEncoder passwordEncoder,
                        AuditService auditService) {
        this.userRepository = userRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
        this.parentRepository = parentRepository;
        this.parentStudentRepository = parentStudentRepository;
        this.classRepository = classRepository;
        this.sectionRepository = sectionRepository;
        this.subjectRepository = subjectRepository;
        this.academicYearRepository = academicYearRepository;
        this.teacherAssignmentRepository = teacherAssignmentRepository;
        this.attendanceRepository = attendanceRepository;
        this.studentFeeRepository = studentFeeRepository;
        this.examRepository = examRepository;
        this.noticeRepository = noticeRepository;
        this.auditLogRepository = auditLogRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public AdminDashboardDTO getDashboardStats() {
        AdminDashboardDTO stats = new AdminDashboardDTO();
        stats.setTotalStudents(studentRepository.count());
        stats.setTotalTeachers(teacherRepository.count());
        stats.setTotalParents(parentRepository.count());
        stats.setTotalClasses(classRepository.count());

        LocalDate today = LocalDate.now();
        long presentCount = attendanceRepository.countPresentOnDate(today);
        long totalAttendanceRecords = attendanceRepository.countTotalOnDate(today);
        double attendancePercent = totalAttendanceRecords > 0 ? (presentCount * 100.0 / totalAttendanceRecords) : 100.0;
        stats.setTodayAttendancePercentage(Math.round(attendancePercent * 10.0) / 10.0);

        Double pendingFees = studentFeeRepository.sumPendingFees();
        stats.setTotalPendingFees(pendingFees != null ? pendingFees : 0.0);

        Double collectedFees = studentFeeRepository.sumCollectedFees();
        stats.setTotalCollectedFees(collectedFees != null ? collectedFees : 0.0);

        stats.setUpcomingExamsCount(examRepository.count());
        stats.setActiveNoticesCount(noticeRepository.count());

        List<NoticeResponseDTO> recentNotices = noticeRepository.findAllByOrderByPublishedDateDesc().stream()
                .limit(5)
                .map(this::convertNoticeToDTO)
                .collect(Collectors.toList());
        stats.setRecentNotices(recentNotices);

        stats.setRecentAuditLogs(auditService.getRecentLogs().stream().limit(5).collect(Collectors.toList()));

        return stats;
    }

    // ==================== TEACHER MANAGEMENT ====================

    @Transactional(readOnly = true)
    public List<TeacherResponseDTO> getAllTeachers() {
        return teacherRepository.findAll().stream()
                .map(this::convertTeacherToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TeacherResponseDTO createTeacher(TeacherCreateRequest request, Long adminUserId, String ipAddress) {
        if (teacherRepository.findByEmployeeId(request.getEmployeeId()).isPresent()) {
            throw new BadRequestException("Teacher with Employee ID '" + request.getEmployeeId() + "' already exists");
        }

        String username = request.getEmployeeId().toUpperCase();
        if (userRepository.existsByUsername(username)) {
            throw new BadRequestException("User account for '" + username + "' already exists");
        }

        String rawTempPassword = PasswordGenerator.generateTemporaryPassword();
        User user = new User(username, passwordEncoder.encode(rawTempPassword), RoleType.ROLE_TEACHER);
        user.setFirstLogin(true);
        user = userRepository.save(user);

        Teacher teacher = new Teacher();
        teacher.setUser(user);
        teacher.setEmployeeId(request.getEmployeeId());
        teacher.setName(request.getName());
        teacher.setEmail(request.getEmail());
        teacher.setPhone(request.getPhone());
        teacher.setGender(request.getGender());
        teacher.setDateOfBirth(request.getDateOfBirth());
        teacher.setAddress(request.getAddress());
        teacher.setQualification(request.getQualification());
        teacher.setJoiningDate(request.getJoiningDate() != null ? request.getJoiningDate() : LocalDate.now());
        teacher.setDepartment(request.getDepartment());
        teacher.setStatus(TeacherStatus.ACTIVE);

        teacher = teacherRepository.save(teacher);

        // Assign Sections/Subjects if provided
        if (request.getSectionIds() != null) {
            for (Long sectionId : request.getSectionIds()) {
                Section section = sectionRepository.findById(sectionId).orElse(null);
                if (section != null) {
                    if (request.getSubjectIds() != null && !request.getSubjectIds().isEmpty()) {
                        for (Long subId : request.getSubjectIds()) {
                            Subject subject = subjectRepository.findById(subId).orElse(null);
                            TeacherAssignment assignment = new TeacherAssignment(teacher, section, subject, false);
                            teacherAssignmentRepository.save(assignment);
                        }
                    } else {
                        TeacherAssignment assignment = new TeacherAssignment(teacher, section, null, false);
                        teacherAssignmentRepository.save(assignment);
                    }
                }
            }
        }

        auditService.log(adminUserId, "admin", "CREATE_TEACHER", "Teacher", teacher.getId(),
                "Created teacher " + teacher.getName() + " (" + teacher.getEmployeeId() + ")", ipAddress);

        TeacherResponseDTO response = convertTeacherToDTO(teacher);
        response.setTemporaryPassword(rawTempPassword);
        return response;
    }

    @Transactional
    public TeacherResponseDTO updateTeacher(Long teacherId, TeacherCreateRequest request, Long adminUserId, String ipAddress) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));

        teacher.setName(request.getName());
        teacher.setEmail(request.getEmail());
        teacher.setPhone(request.getPhone());
        teacher.setGender(request.getGender());
        teacher.setDateOfBirth(request.getDateOfBirth());
        teacher.setAddress(request.getAddress());
        teacher.setQualification(request.getQualification());
        teacher.setDepartment(request.getDepartment());

        teacherRepository.save(teacher);

        auditService.log(adminUserId, "admin", "UPDATE_TEACHER", "Teacher", teacher.getId(),
                "Updated teacher profile " + teacher.getName(), ipAddress);

        return convertTeacherToDTO(teacher);
    }

    // ==================== STUDENT MANAGEMENT ====================

    @Transactional(readOnly = true)
    public List<StudentResponseDTO> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(this::convertStudentToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public StudentResponseDTO createStudent(StudentCreateRequest request, Long performedByUserId, String ipAddress) {
        if (studentRepository.findByAdmissionNumber(request.getAdmissionNumber()).isPresent()) {
            throw new BadRequestException("Student with Admission Number '" + request.getAdmissionNumber() + "' already exists");
        }

        Section section = sectionRepository.findById(request.getSectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Section not found"));

        // Generate unique Student ID e.g. STU20260025
        String yearPrefix = String.valueOf(LocalDate.now().getYear());
        long count = studentRepository.count() + 1;
        String studentId = String.format("STU%s%04d", yearPrefix, count);

        while (studentRepository.findByStudentId(studentId).isPresent()) {
            count++;
            studentId = String.format("STU%s%04d", yearPrefix, count);
        }

        String rawTempPassword = PasswordGenerator.generateTemporaryPassword();
        User studentUser = new User(studentId, passwordEncoder.encode(rawTempPassword), RoleType.ROLE_STUDENT);
        studentUser.setFirstLogin(true);
        studentUser = userRepository.save(studentUser);

        Student student = new Student();
        student.setUser(studentUser);
        student.setStudentId(studentId);
        student.setAdmissionNumber(request.getAdmissionNumber());
        student.setName(request.getName());
        student.setDateOfBirth(request.getDateOfBirth());
        student.setGender(request.getGender());
        student.setBloodGroup(request.getBloodGroup());
        student.setPhone(request.getPhone());
        student.setEmail(request.getEmail());
        student.setAddress(request.getAddress());
        student.setAdmissionDate(request.getAdmissionDate() != null ? request.getAdmissionDate() : LocalDate.now());
        student.setSection(section);
        student.setRollNumber(request.getRollNumber());
        student.setStatus(StudentStatus.ACTIVE);

        student = studentRepository.save(student);

        // Handle Parent Account Creation / Linking
        if (request.getParentName() != null && !request.getParentName().trim().isEmpty() &&
            request.getParentPhone() != null && !request.getParentPhone().trim().isEmpty()) {
            
            String parentUsername = "PAR" + request.getParentPhone().replaceAll("\\D", "");
            User parentUser = userRepository.findByUsername(parentUsername).orElseGet(() -> {
                String parentTempPass = PasswordGenerator.generateTemporaryPassword();
                User pUser = new User(parentUsername, passwordEncoder.encode(parentTempPass), RoleType.ROLE_PARENT);
                pUser.setFirstLogin(true);
                return userRepository.save(pUser);
            });

            Parent parent = parentRepository.findByUserId(parentUser.getId()).orElseGet(() -> {
                Parent p = new Parent();
                p.setUser(parentUser);
                p.setName(request.getParentName());
                p.setPhone(request.getParentPhone());
                p.setEmail(request.getParentEmail());
                p.setOccupation(request.getParentOccupation());
                p.setRelationship(request.getParentRelationship() != null ? request.getParentRelationship() : "Parent");
                return parentRepository.save(p);
            });

            if (!parentStudentRepository.existsByParentIdAndStudentId(parent.getId(), student.getId())) {
                parentStudentRepository.save(new ParentStudent(parent, student));
            }
        }

        auditService.log(performedByUserId, "user", "CREATE_STUDENT", "Student", student.getId(),
                "Created student " + student.getName() + " (" + student.getStudentId() + ") in " + section.getFullName(), ipAddress);

        StudentResponseDTO response = convertStudentToDTO(student);
        response.setTemporaryPassword(rawTempPassword);
        return response;
    }

    @Transactional
    public StudentResponseDTO updateStudent(Long studentId, StudentUpdateRequest request, Long performedByUserId, String ipAddress) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        if (request.getName() != null) student.setName(request.getName());
        if (request.getDateOfBirth() != null) student.setDateOfBirth(request.getDateOfBirth());
        if (request.getGender() != null) student.setGender(request.getGender());
        if (request.getBloodGroup() != null) student.setBloodGroup(request.getBloodGroup());
        if (request.getPhone() != null) student.setPhone(request.getPhone());
        if (request.getEmail() != null) student.setEmail(request.getEmail());
        if (request.getAddress() != null) student.setAddress(request.getAddress());
        if (request.getRollNumber() != null) student.setRollNumber(request.getRollNumber());
        if (request.getStatus() != null) student.setStatus(request.getStatus());

        if (request.getSectionId() != null && !request.getSectionId().equals(student.getSection().getId())) {
            Section newSection = sectionRepository.findById(request.getSectionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Target section not found"));
            student.setSection(newSection);
        }

        student = studentRepository.save(student);

        auditService.log(performedByUserId, "user", "UPDATE_STUDENT", "Student", student.getId(),
                "Updated student record for " + student.getName(), ipAddress);

        return convertStudentToDTO(student);
    }

    // ==================== PASSWORD RESET (CONTROLLED) ====================

    @Transactional
    public String resetUserPassword(Long userId, Long performedByUserId, String ipAddress) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User account not found"));

        String rawTempPassword = PasswordGenerator.generateTemporaryPassword();
        user.setPassword(passwordEncoder.encode(rawTempPassword));
        user.setFirstLogin(true);
        userRepository.save(user);

        auditService.log(performedByUserId, "authorized_staff", "RESET_PASSWORD", "User", user.getId(),
                "Authorized reset temporary password generated for username: " + user.getUsername(), ipAddress);

        return rawTempPassword;
    }

    // ==================== CLASS & SECTION MANAGEMENT ====================

    @Transactional(readOnly = true)
    public List<ClassResponseDTO> getAllClasses() {
        return classRepository.findAll().stream()
                .map(this::convertClassToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public SchoolClass createClass(String className, Long academicYearId) {
        AcademicYear academicYear = academicYearRepository.findById(academicYearId)
                .orElseGet(() -> academicYearRepository.findByIsCurrentTrue()
                        .orElseThrow(() -> new BadRequestException("Active academic year required")));

        SchoolClass schoolClass = new SchoolClass(className, academicYear);
        return classRepository.save(schoolClass);
    }

    @Transactional
    public SectionResponseDTO createSection(SectionCreateRequest request) {
        SchoolClass schoolClass = classRepository.findById(request.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));

        Teacher classTeacher = null;
        if (request.getClassTeacherId() != null) {
            classTeacher = teacherRepository.findById(request.getClassTeacherId()).orElse(null);
        }

        Section section = new Section(schoolClass, request.getSectionName(), request.getRoomNumber(), request.getCapacity(), classTeacher);
        section = sectionRepository.save(section);

        if (classTeacher != null) {
            TeacherAssignment assignment = new TeacherAssignment(classTeacher, section, null, true);
            teacherAssignmentRepository.save(assignment);
        }

        return convertSectionToDTO(section);
    }

    // ==================== DTO CONVERTERS ====================

    public TeacherResponseDTO convertTeacherToDTO(Teacher teacher) {
        TeacherResponseDTO dto = new TeacherResponseDTO();
        dto.setId(teacher.getId());
        dto.setUserId(teacher.getUser().getId());
        dto.setEmployeeId(teacher.getEmployeeId());
        dto.setName(teacher.getName());
        dto.setDateOfBirth(teacher.getDateOfBirth());
        dto.setGender(teacher.getGender());
        dto.setPhone(teacher.getPhone());
        dto.setEmail(teacher.getEmail());
        dto.setAddress(teacher.getAddress());
        dto.setQualification(teacher.getQualification());
        dto.setJoiningDate(teacher.getJoiningDate());
        dto.setDepartment(teacher.getDepartment());
        dto.setStatus(teacher.getStatus().name());

        List<TeacherAssignment> assignments = teacherAssignmentRepository.findByTeacherId(teacher.getId());
        dto.setAssignedClasses(assignments.stream()
                .map(a -> a.getSection().getFullName() + (a.isClassTeacher() ? " (Class Teacher)" : ""))
                .distinct().collect(Collectors.toList()));
        dto.setAssignedSubjects(assignments.stream()
                .filter(a -> a.getSubject() != null)
                .map(a -> a.getSubject().getSubjectName())
                .distinct().collect(Collectors.toList()));

        List<SectionResponseDTO> sections = assignments.stream()
                .map(a -> convertSectionToDTO(a.getSection()))
                .distinct().collect(Collectors.toList());
        dto.setSections(sections);

        return dto;
    }

    public StudentResponseDTO convertStudentToDTO(Student student) {
        StudentResponseDTO dto = new StudentResponseDTO();
        dto.setId(student.getId());
        dto.setUserId(student.getUser().getId());
        dto.setStudentId(student.getStudentId());
        dto.setAdmissionNumber(student.getAdmissionNumber());
        dto.setName(student.getName());
        dto.setDateOfBirth(student.getDateOfBirth());
        dto.setGender(student.getGender());
        dto.setBloodGroup(student.getBloodGroup());
        dto.setPhone(student.getPhone());
        dto.setEmail(student.getEmail());
        dto.setAddress(student.getAddress());
        dto.setAdmissionDate(student.getAdmissionDate());
        dto.setRollNumber(student.getRollNumber());
        dto.setStatus(student.getStatus().name());

        if (student.getSection() != null) {
            dto.setSectionId(student.getSection().getId());
            dto.setSectionName(student.getSection().getSectionName());
            if (student.getSection().getSchoolClass() != null) {
                dto.setClassName(student.getSection().getSchoolClass().getClassName());
                dto.setClassSectionFullName(student.getSection().getFullName());
                if (student.getSection().getSchoolClass().getAcademicYear() != null) {
                    dto.setAcademicYearName(student.getSection().getSchoolClass().getAcademicYear().getYearName());
                }
            }
        }

        List<ParentStudent> parentLinks = parentStudentRepository.findByStudentId(student.getId());
        if (!parentLinks.isEmpty()) {
            Parent parent = parentLinks.get(0).getParent();
            dto.setParentName(parent.getName());
            dto.setParentPhone(parent.getPhone());
        }

        long total = attendanceRepository.countByStudentId(student.getId());
        long present = attendanceRepository.countByStudentIdAndStatus(student.getId(), AttendanceStatus.PRESENT);
        dto.setAttendancePercentage(total > 0 ? Math.round((present * 100.0 / total) * 10.0) / 10.0 : 100.0);

        return dto;
    }

    public SectionResponseDTO convertSectionToDTO(Section section) {
        SectionResponseDTO dto = new SectionResponseDTO();
        dto.setId(section.getId());
        dto.setSectionName(section.getSectionName());
        dto.setFullName(section.getFullName());
        dto.setRoomNumber(section.getRoomNumber());
        dto.setCapacity(section.getCapacity());

        if (section.getSchoolClass() != null) {
            dto.setClassId(section.getSchoolClass().getId());
            dto.setClassName(section.getSchoolClass().getClassName());
        }

        if (section.getClassTeacher() != null) {
            dto.setClassTeacherId(section.getClassTeacher().getId());
            dto.setClassTeacherName(section.getClassTeacher().getName());
        }

        dto.setStudentCount(studentRepository.countBySectionId(section.getId()));
        return dto;
    }

    public ClassResponseDTO convertClassToDTO(SchoolClass schoolClass) {
        ClassResponseDTO dto = new ClassResponseDTO();
        dto.setId(schoolClass.getId());
        dto.setClassName(schoolClass.getClassName());
        if (schoolClass.getAcademicYear() != null) {
            dto.setAcademicYearId(schoolClass.getAcademicYear().getId());
            dto.setAcademicYearName(schoolClass.getAcademicYear().getYearName());
        }
        List<Section> sections = sectionRepository.findBySchoolClassId(schoolClass.getId());
        dto.setSections(sections.stream().map(this::convertSectionToDTO).collect(Collectors.toList()));
        return dto;
    }

    private NoticeResponseDTO convertNoticeToDTO(Notice notice) {
        NoticeResponseDTO dto = new NoticeResponseDTO();
        dto.setId(notice.getId());
        dto.setTitle(notice.getTitle());
        dto.setContent(notice.getContent());
        dto.setAudience(notice.getAudience());
        dto.setPublishedDate(notice.getPublishedDate());
        dto.setExpiryDate(notice.getExpiryDate());
        dto.setAttachmentUrl(notice.getAttachmentUrl());
        dto.setCreatedAt(notice.getCreatedAt());
        if (notice.getCreatedByUser() != null) {
            dto.setAuthorName(notice.getCreatedByUser().getUsername());
        }
        if (notice.getSection() != null) {
            dto.setSectionId(notice.getSection().getId());
            dto.setSectionFullName(notice.getSection().getFullName());
        }
        return dto;
    }
}
