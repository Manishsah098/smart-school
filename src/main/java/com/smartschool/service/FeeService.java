package com.smartschool.service;

import com.smartschool.dto.FeeCreateRequest;
import com.smartschool.dto.FeePaymentRequest;
import com.smartschool.dto.FeeResponseDTO;
import com.smartschool.dto.StudentFeeDTO;
import com.smartschool.entity.*;
import com.smartschool.entity.enums.FeeStatus;
import com.smartschool.exception.BadRequestException;
import com.smartschool.exception.ResourceNotFoundException;
import com.smartschool.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FeeService {

    private final FeeRepository feeRepository;
    private final StudentFeeRepository studentFeeRepository;
    private final FeePaymentRepository feePaymentRepository;
    private final AcademicYearRepository academicYearRepository;
    private final SchoolClassRepository classRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final NoticeNotificationService notificationService;

    public FeeService(FeeRepository feeRepository,
                      StudentFeeRepository studentFeeRepository,
                      FeePaymentRepository feePaymentRepository,
                      AcademicYearRepository academicYearRepository,
                      SchoolClassRepository classRepository,
                      StudentRepository studentRepository,
                      UserRepository userRepository,
                      AuditService auditService,
                      NoticeNotificationService notificationService) {
        this.feeRepository = feeRepository;
        this.studentFeeRepository = studentFeeRepository;
        this.feePaymentRepository = feePaymentRepository;
        this.academicYearRepository = academicYearRepository;
        this.classRepository = classRepository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    @Transactional
    public FeeResponseDTO createFee(FeeCreateRequest request, Long adminUserId, String ipAddress) {
        AcademicYear academicYear = academicYearRepository.findById(request.getAcademicYearId())
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found"));

        SchoolClass schoolClass = null;
        if (request.getClassId() != null) {
            schoolClass = classRepository.findById(request.getClassId())
                    .orElseThrow(() -> new ResourceNotFoundException("Class not found"));
        }

        Fee fee = new Fee();
        fee.setAcademicYear(academicYear);
        fee.setSchoolClass(schoolClass);
        fee.setTitle(request.getTitle());
        fee.setFeeType(request.getFeeType());
        fee.setAmount(request.getAmount());
        fee.setDueDate(request.getDueDate());

        fee = feeRepository.save(fee);

        // Assign fee to applicable students
        List<Student> students;
        if (schoolClass != null) {
            students = studentRepository.findAll().stream()
                    .filter(s -> s.getSection() != null && s.getSection().getSchoolClass().getId().equals(request.getClassId()))
                    .collect(Collectors.toList());
        } else {
            students = studentRepository.findAll();
        }

        for (Student student : students) {
            StudentFee sf = new StudentFee(fee, student, fee.getAmount());
            studentFeeRepository.save(sf);

            notificationService.createNotification(
                    student.getUser().getId(),
                    "Fee Notice: " + fee.getTitle(),
                    "Fee of ₹" + fee.getAmount() + " is due on " + fee.getDueDate(),
                    "FEE",
                    "/student/fees"
            );
        }

        auditService.log(adminUserId, "admin", "CREATE_FEE", "Fee", fee.getId(),
                "Created fee: " + fee.getTitle() + " (Amount: ₹" + fee.getAmount() + ")", ipAddress);

        return convertFeeToDTO(fee);
    }

    @Transactional(readOnly = true)
    public List<FeeResponseDTO> getAllFees() {
        return feeRepository.findAll().stream()
                .map(this::convertFeeToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StudentFeeDTO> getStudentFees(Long studentId) {
        return studentFeeRepository.findByStudentId(studentId).stream()
                .map(this::convertStudentFeeToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public StudentFeeDTO recordPayment(FeePaymentRequest request, Long staffUserId, String ipAddress) {
        StudentFee studentFee = studentFeeRepository.findById(request.getStudentFeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Student fee record not found"));

        double pending = studentFee.getPendingAmount();
        if (request.getAmount() > pending) {
            throw new BadRequestException("Payment amount (₹" + request.getAmount() + ") cannot exceed pending balance (₹" + pending + ")");
        }

        User staff = userRepository.findById(staffUserId).orElse(null);
        String staffName = staff != null ? staff.getUsername() : "School Office";

        String receiptNo = request.getReceiptNumber();
        if (receiptNo == null || receiptNo.trim().isEmpty()) {
            receiptNo = "REC" + LocalDate.now().getYear() + String.format("%05d", (int)(Math.random() * 90000) + 10000);
        }

        FeePayment payment = new FeePayment();
        payment.setStudentFee(studentFee);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "CASH");
        payment.setReceiptNumber(receiptNo);
        payment.setReceivedBy(staffName);

        feePaymentRepository.save(payment);

        double newPaid = studentFee.getPaidAmount() + request.getAmount();
        studentFee.setPaidAmount(newPaid);

        if (newPaid >= studentFee.getTotalAmount()) {
            studentFee.setStatus(FeeStatus.PAID);
        } else if (newPaid > 0) {
            studentFee.setStatus(FeeStatus.PARTIAL);
        }

        studentFee = studentFeeRepository.save(studentFee);

        auditService.log(staffUserId, staffName, "COLLECT_FEE", "FeePayment", payment.getId(),
                "Collected ₹" + payment.getAmount() + " from student " + studentFee.getStudent().getName() + " (Receipt: " + receiptNo + ")", ipAddress);

        return convertStudentFeeToDTO(studentFee);
    }

    public FeeResponseDTO convertFeeToDTO(Fee fee) {
        FeeResponseDTO dto = new FeeResponseDTO();
        dto.setId(fee.getId());
        dto.setAcademicYearId(fee.getAcademicYear().getId());
        dto.setAcademicYearName(fee.getAcademicYear().getYearName());
        if (fee.getSchoolClass() != null) {
            dto.setClassId(fee.getSchoolClass().getId());
            dto.setClassName(fee.getSchoolClass().getClassName());
        }
        dto.setTitle(fee.getTitle());
        dto.setFeeType(fee.getFeeType());
        dto.setAmount(fee.getAmount());
        dto.setDueDate(fee.getDueDate());
        return dto;
    }

    public StudentFeeDTO convertStudentFeeToDTO(StudentFee sf) {
        StudentFeeDTO dto = new StudentFeeDTO();
        dto.setId(sf.getId());
        dto.setFeeId(sf.getFee().getId());
        dto.setTitle(sf.getFee().getTitle());
        dto.setFeeType(sf.getFee().getFeeType());
        dto.setDueDate(sf.getFee().getDueDate());
        dto.setStudentId(sf.getStudent().getId());
        dto.setStudentName(sf.getStudent().getName());
        dto.setAdmissionNumber(sf.getStudent().getAdmissionNumber());
        dto.setTotalAmount(sf.getTotalAmount());
        dto.setPaidAmount(sf.getPaidAmount());
        dto.setPendingAmount(sf.getPendingAmount());
        dto.setStatus(sf.getStatus());

        List<StudentFeeDTO.PaymentRecordDTO> payments = feePaymentRepository.findByStudentFeeId(sf.getId()).stream()
                .map(p -> new StudentFeeDTO.PaymentRecordDTO(p.getId(), p.getPaymentDate(), p.getAmount(), p.getPaymentMethod(), p.getReceiptNumber(), p.getReceivedBy()))
                .collect(Collectors.toList());
        dto.setPaymentHistory(payments);

        return dto;
    }
}
