package com.smartschool.service;

import com.smartschool.dto.AttendanceSummaryDTO;
import com.smartschool.dto.StudentFeeDTO;
import com.smartschool.dto.StudentResponseDTO;
import com.smartschool.dto.StudentResultCardDTO;
import com.smartschool.entity.SchoolClass;
import com.smartschool.entity.Section;
import com.smartschool.entity.Student;
import com.smartschool.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final StudentRepository studentRepository;
    private final SectionRepository sectionRepository;
    private final SchoolClassRepository classRepository;
    private final AttendanceService attendanceService;
    private final ExamResultService examResultService;
    private final FeeService feeService;
    private final AdminService adminService;

    public ReportService(StudentRepository studentRepository,
                         SectionRepository sectionRepository,
                         SchoolClassRepository classRepository,
                         AttendanceService attendanceService,
                         ExamResultService examResultService,
                         FeeService feeService,
                         AdminService adminService) {
        this.studentRepository = studentRepository;
        this.sectionRepository = sectionRepository;
        this.classRepository = classRepository;
        this.attendanceService = attendanceService;
        this.examResultService = examResultService;
        this.feeService = feeService;
        this.adminService = adminService;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> generateSectionAttendanceReport(Long sectionId) {
        Section section = sectionRepository.findById(sectionId).orElse(null);
        List<Student> students = studentRepository.findBySectionId(sectionId);

        List<AttendanceSummaryDTO> summaries = students.stream()
                .map(s -> attendanceService.getStudentAttendanceSummary(s.getId()))
                .collect(Collectors.toList());

        double sectionAvg = summaries.stream().mapToDouble(AttendanceSummaryDTO::getPercentage).average().orElse(100.0);

        Map<String, Object> report = new HashMap<>();
        report.put("sectionId", sectionId);
        report.put("sectionName", section != null ? section.getFullName() : "");
        report.put("studentCount", students.size());
        report.put("averageAttendancePercentage", Math.round(sectionAvg * 10.0) / 10.0);
        report.put("students", summaries);
        return report;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> generateExamReport(Long examId, Long sectionId) {
        List<Student> students = studentRepository.findBySectionId(sectionId);
        List<StudentResultCardDTO> results = students.stream()
                .map(s -> examResultService.getStudentResultCard(s.getId(), examId))
                .collect(Collectors.toList());

        double classAvg = results.stream().mapToDouble(StudentResultCardDTO::getPercentage).average().orElse(0.0);
        long passCount = results.stream().filter(r -> "PASS".equals(r.getResultStatus())).count();

        Map<String, Object> report = new HashMap<>();
        report.put("examId", examId);
        report.put("sectionId", sectionId);
        report.put("totalStudents", students.size());
        report.put("passedStudents", passCount);
        report.put("failedStudents", students.size() - passCount);
        report.put("averagePercentage", Math.round(classAvg * 10.0) / 10.0);
        report.put("results", results);
        return report;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> generateFeeCollectionReport() {
        List<Student> students = studentRepository.findAll();
        List<StudentFeeDTO> allFees = students.stream()
                .flatMap(s -> feeService.getStudentFees(s.getId()).stream())
                .collect(Collectors.toList());

        double totalAmount = allFees.stream().mapToDouble(StudentFeeDTO::getTotalAmount).sum();
        double paidAmount = allFees.stream().mapToDouble(StudentFeeDTO::getPaidAmount).sum();
        double pendingAmount = allFees.stream().mapToDouble(StudentFeeDTO::getPendingAmount).sum();

        Map<String, Object> report = new HashMap<>();
        report.put("totalAssignedFees", totalAmount);
        report.put("totalCollectedFees", paidAmount);
        report.put("totalPendingFees", pendingAmount);
        report.put("feeRecords", allFees);
        return report;
    }
}
