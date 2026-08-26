package com.smartschool.config;

import com.smartschool.entity.*;
import com.smartschool.entity.enums.*;
import com.smartschool.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    private final ParentStudentRepository parentStudentRepository;
    private final AcademicYearRepository academicYearRepository;
    private final SchoolClassRepository classRepository;
    private final SectionRepository sectionRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final TimetableRepository timetableRepository;
    private final ExamRepository examRepository;
    private final ExamScheduleRepository examScheduleRepository;
    private final MarkRepository markRepository;
    private final FeeRepository feeRepository;
    private final StudentFeeRepository studentFeeRepository;
    private final FeePaymentRepository feePaymentRepository;
    private final HomeworkRepository homeworkRepository;
    private final HomeworkSubmissionRepository homeworkSubmissionRepository;
    private final AttendanceRepository attendanceRepository;
    private final NoticeRepository noticeRepository;
    private final EventRepository eventRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.init.admin.username:admin}")
    private String adminUsername;

    @Value("${app.init.admin.password:Admin@123}")
    private String adminPassword;

    @Value("${app.init.seed-sample-data:true}")
    private boolean seedSampleData;

    public DatabaseInitializer(UserRepository userRepository,
                               TeacherRepository teacherRepository,
                               StudentRepository studentRepository,
                               ParentRepository parentRepository,
                               ParentStudentRepository parentStudentRepository,
                               AcademicYearRepository academicYearRepository,
                               SchoolClassRepository classRepository,
                               SectionRepository sectionRepository,
                               SubjectRepository subjectRepository,
                               TeacherAssignmentRepository teacherAssignmentRepository,
                               TimetableRepository timetableRepository,
                               ExamRepository examRepository,
                               ExamScheduleRepository examScheduleRepository,
                               MarkRepository markRepository,
                               FeeRepository feeRepository,
                               StudentFeeRepository studentFeeRepository,
                               FeePaymentRepository feePaymentRepository,
                               HomeworkRepository homeworkRepository,
                               HomeworkSubmissionRepository homeworkSubmissionRepository,
                               AttendanceRepository attendanceRepository,
                               NoticeRepository noticeRepository,
                               EventRepository eventRepository,
                               PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
        this.parentRepository = parentRepository;
        this.parentStudentRepository = parentStudentRepository;
        this.academicYearRepository = academicYearRepository;
        this.classRepository = classRepository;
        this.sectionRepository = sectionRepository;
        this.subjectRepository = subjectRepository;
        this.teacherAssignmentRepository = teacherAssignmentRepository;
        this.timetableRepository = timetableRepository;
        this.examRepository = examRepository;
        this.examScheduleRepository = examScheduleRepository;
        this.markRepository = markRepository;
        this.feeRepository = feeRepository;
        this.studentFeeRepository = studentFeeRepository;
        this.feePaymentRepository = feePaymentRepository;
        this.homeworkRepository = homeworkRepository;
        this.homeworkSubmissionRepository = homeworkSubmissionRepository;
        this.attendanceRepository = attendanceRepository;
        this.noticeRepository = noticeRepository;
        this.eventRepository = eventRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        logger.info("Initializing SmartSchool database system...");

        // 1. Initial Admin Account
        User adminUser;
        if (!userRepository.existsByUsername(adminUsername)) {
            adminUser = new User(adminUsername, passwordEncoder.encode(adminPassword), RoleType.ROLE_ADMIN);
            adminUser.setFirstLogin(false);
            adminUser = userRepository.save(adminUser);
            logger.info("Created Initial School Administrator account: {}", adminUsername);
        } else {
            adminUser = userRepository.findByUsername(adminUsername).get();
        }

        if (!seedSampleData || studentRepository.count() > 0) {
            logger.info("Seed data skipped or already initialized.");
            return;
        }

        logger.info("Seeding realistic sample academic structure and accounts...");

        // 2. Academic Year
        AcademicYear academicYear = new AcademicYear("2026-2027", LocalDate.of(2026, 4, 1), LocalDate.of(2027, 3, 31), true);
        academicYear = academicYearRepository.save(academicYear);

        // 3. Subjects
        Subject math = subjectRepository.save(new Subject("Mathematics", "MATH101"));
        Subject science = subjectRepository.save(new Subject("Science", "SCI101"));
        Subject english = subjectRepository.save(new Subject("English", "ENG101"));
        Subject social = subjectRepository.save(new Subject("Social Science", "SOC101"));
        Subject computer = subjectRepository.save(new Subject("Computer Science", "CS101"));

        // 4. Teachers
        User teacherUser1 = new User("T001", passwordEncoder.encode("Teacher@123"), RoleType.ROLE_TEACHER);
        teacherUser1.setFirstLogin(false);
        teacherUser1 = userRepository.save(teacherUser1);

        Teacher teacher1 = new Teacher();
        teacher1.setUser(teacherUser1);
        teacher1.setEmployeeId("T001");
        teacher1.setName("Mr. Ravi Sharma");
        teacher1.setEmail("ravi.sharma@smartschool.edu");
        teacher1.setPhone("+91 98765 43211");
        teacher1.setGender("Male");
        teacher1.setQualification("M.Sc., B.Ed.");
        teacher1.setDepartment("Mathematics");
        teacher1.setJoiningDate(LocalDate.of(2022, 6, 1));
        teacher1 = teacherRepository.save(teacher1);

        User teacherUser2 = new User("T002", passwordEncoder.encode("Teacher@123"), RoleType.ROLE_TEACHER);
        teacherUser2.setFirstLogin(false);
        teacherUser2 = userRepository.save(teacherUser2);

        Teacher teacher2 = new Teacher();
        teacher2.setUser(teacherUser2);
        teacher2.setEmployeeId("T002");
        teacher2.setName("Mrs. Sunita Verma");
        teacher2.setEmail("sunita.verma@smartschool.edu");
        teacher2.setPhone("+91 98765 43212");
        teacher2.setGender("Female");
        teacher2.setQualification("M.Sc. Physics, B.Ed.");
        teacher2.setDepartment("Science");
        teacher2.setJoiningDate(LocalDate.of(2023, 7, 1));
        teacher2 = teacherRepository.save(teacher2);

        // 5. Classes & Sections
        SchoolClass class10 = classRepository.save(new SchoolClass("Class 10", academicYear));
        SchoolClass class9 = classRepository.save(new SchoolClass("Class 9", academicYear));

        Section sec10A = sectionRepository.save(new Section(class10, "A", "Room 101", 40, teacher1));
        Section sec10B = sectionRepository.save(new Section(class10, "B", "Room 102", 40, teacher2));
        Section sec9A = sectionRepository.save(new Section(class9, "A", "Room 201", 40, null));

        // 6. Teacher Assignments
        teacherAssignmentRepository.save(new TeacherAssignment(teacher1, sec10A, math, true));
        teacherAssignmentRepository.save(new TeacherAssignment(teacher1, sec10A, computer, false));
        teacherAssignmentRepository.save(new TeacherAssignment(teacher1, sec10B, math, false));
        teacherAssignmentRepository.save(new TeacherAssignment(teacher2, sec10B, science, true));
        teacherAssignmentRepository.save(new TeacherAssignment(teacher2, sec10A, science, false));

        // 7. Parent Account
        User parentUser = new User("PAR9876543210", passwordEncoder.encode("Parent@123"), RoleType.ROLE_PARENT);
        parentUser.setFirstLogin(false);
        parentUser = userRepository.save(parentUser);

        Parent parent = new Parent();
        parent.setUser(parentUser);
        parent.setName("Mr. Rajesh Sharma");
        parent.setPhone("9876543210");
        parent.setEmail("rajesh.sharma@example.com");
        parent.setOccupation("Civil Engineer");
        parent.setAddress("742 Evergreen Terrace, Springfield");
        parent.setRelationship("Father");
        parent = parentRepository.save(parent);

        // 8. Students
        Student student1 = createSampleStudent("STU20260001", "ADM-1001", "Rahul Sharma", sec10A, 1, "Male", "O+", "9876543210", "Student@123");
        Student student2 = createSampleStudent("STU20260002", "ADM-1002", "Ankit Patel", sec10A, 2, "Male", "B+", "9876543220", "Student@123");
        Student student3 = createSampleStudent("STU20260003", "ADM-1003", "Priya Singh", sec10A, 3, "Female", "A+", "9876543230", "Student@123");
        Student student4 = createSampleStudent("STU20260004", "ADM-1004", "Neha Gupta", sec9A, 1, "Female", "AB+", "9876543240", "Student@123");

        // Link Parent to Student 1 (Rahul) and Student 3 (Priya)
        parentStudentRepository.save(new ParentStudent(parent, student1));
        parentStudentRepository.save(new ParentStudent(parent, student3));

        // 9. Attendance
        LocalDate today = LocalDate.now();
        for (int i = 10; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            if (d.getDayOfWeek().getValue() < 7) { // Mon-Sat
                attendanceRepository.save(new Attendance(student1, sec10A, d, (i == 3 ? AttendanceStatus.LATE : AttendanceStatus.PRESENT), "Regular", teacher1));
                attendanceRepository.save(new Attendance(student2, sec10A, d, (i == 4 || i == 8 ? AttendanceStatus.ABSENT : AttendanceStatus.PRESENT), "Regular", teacher1));
                attendanceRepository.save(new Attendance(student3, sec10A, d, AttendanceStatus.PRESENT, "Regular", teacher1));
            }
        }

        // 10. Homework
        Homework hw1 = new Homework();
        hw1.setSection(sec10A);
        hw1.setSubject(math);
        hw1.setTeacher(teacher1);
        hw1.setTitle("Trigonometry Practice Sheet 4");
        hw1.setDescription("Complete exercise 4.2 questions 1 to 15 from NCERT textbook.");
        hw1.setAssignedDate(today.minusDays(2));
        hw1.setDueDate(today.plusDays(3));
        hw1 = homeworkRepository.save(hw1);

        Homework hw2 = new Homework();
        hw2.setSection(sec10A);
        hw2.setSubject(science);
        hw2.setTeacher(teacher2);
        hw2.setTitle("Chemical Reactions & Equations Lab Summary");
        hw2.setDescription("Write down the balanced chemical equations for experiment 2.");
        hw2.setAssignedDate(today.minusDays(3));
        hw2.setDueDate(today.plusDays(2));
        hw2 = homeworkRepository.save(hw2);

        // Student 1 submission
        HomeworkSubmission sub1 = new HomeworkSubmission();
        sub1.setHomework(hw1);
        sub1.setStudent(student1);
        sub1.setContent("Solved all 15 questions in the practice notebook. Attached formula sheet references.");
        sub1.setSubmissionDate(LocalDateTime.now().minusDays(1));
        sub1.setStatus(HomeworkStatus.SUBMITTED);
        homeworkSubmissionRepository.save(sub1);

        // 11. Timetable
        createTimetableSlot(sec10A, math, teacher1, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(9, 45), "Room 101");
        createTimetableSlot(sec10A, science, teacher2, DayOfWeek.MONDAY, LocalTime.of(9, 45), LocalTime.of(10, 30), "Science Lab");
        createTimetableSlot(sec10A, english, teacher1, DayOfWeek.MONDAY, LocalTime.of(10, 45), LocalTime.of(11, 30), "Room 101");
        createTimetableSlot(sec10A, computer, teacher1, DayOfWeek.MONDAY, LocalTime.of(11, 30), LocalTime.of(12, 15), "Comp Lab 1");

        createTimetableSlot(sec10A, math, teacher1, DayOfWeek.TUESDAY, LocalTime.of(9, 0), LocalTime.of(9, 45), "Room 101");
        createTimetableSlot(sec10A, science, teacher2, DayOfWeek.TUESDAY, LocalTime.of(9, 45), LocalTime.of(10, 30), "Science Lab");
        createTimetableSlot(sec10A, social, teacher2, DayOfWeek.TUESDAY, LocalTime.of(10, 45), LocalTime.of(11, 30), "Room 101");

        // 12. Examination & Marks
        Exam exam1 = new Exam(academicYear, "Quarterly Examination 2026", "Quarterly", today.minusDays(15), today.minusDays(10), true);
        exam1 = examRepository.save(exam1);

        ExamSchedule schMath = new ExamSchedule();
        schMath.setExam(exam1);
        schMath.setSection(sec10A);
        schMath.setSubject(math);
        schMath.setExamDate(today.minusDays(14));
        schMath.setStartTime(LocalTime.of(9, 30));
        schMath.setEndTime(LocalTime.of(12, 30));
        schMath.setRoomNumber("Room 101");
        schMath.setMaxMarks(100.0);
        schMath.setPassingMarks(35.0);
        schMath = examScheduleRepository.save(schMath);

        ExamSchedule schSci = new ExamSchedule();
        schSci.setExam(exam1);
        schSci.setSection(sec10A);
        schSci.setSubject(science);
        schSci.setExamDate(today.minusDays(12));
        schSci.setStartTime(LocalTime.of(9, 30));
        schSci.setEndTime(LocalTime.of(12, 30));
        schSci.setRoomNumber("Room 101");
        schSci.setMaxMarks(100.0);
        schSci.setPassingMarks(35.0);
        schSci = examScheduleRepository.save(schSci);

        markRepository.save(new Mark(schMath, student1, 92.0, "A+", "Exceptional problem solving", teacher1));
        markRepository.save(new Mark(schSci, student1, 88.0, "A", "Very good understanding", teacher2));

        markRepository.save(new Mark(schMath, student2, 74.0, "B", "Good effort", teacher1));
        markRepository.save(new Mark(schSci, student2, 68.0, "B", "Needs improvement in numericals", teacher2));

        markRepository.save(new Mark(schMath, student3, 85.0, "A", "Great performance", teacher1));
        markRepository.save(new Mark(schSci, student3, 90.0, "A+", "Outstanding work", teacher2));

        // 13. Fees
        Fee fee1 = new Fee();
        fee1.setAcademicYear(academicYear);
        fee1.setSchoolClass(class10);
        fee1.setTitle("Term 1 Tuition Fee");
        fee1.setFeeType(FeeType.TUITION);
        fee1.setAmount(15000.0);
        fee1.setDueDate(today.plusDays(20));
        fee1 = feeRepository.save(fee1);

        StudentFee sf1 = new StudentFee(fee1, student1, 15000.0);
        sf1.setPaidAmount(15000.0);
        sf1.setStatus(FeeStatus.PAID);
        sf1 = studentFeeRepository.save(sf1);

        FeePayment pay1 = new FeePayment();
        pay1.setStudentFee(sf1);
        pay1.setPaymentDate(LocalDateTime.now().minusDays(5));
        pay1.setAmount(15000.0);
        pay1.setPaymentMethod("ONLINE");
        pay1.setReceiptNumber("REC202600101");
        pay1.setReceivedBy("School Portal Online");
        feePaymentRepository.save(pay1);

        StudentFee sf2 = new StudentFee(fee1, student2, 15000.0);
        sf2.setPaidAmount(5000.0);
        sf2.setStatus(FeeStatus.PARTIAL);
        sf2 = studentFeeRepository.save(sf2);

        FeePayment pay2 = new FeePayment();
        pay2.setStudentFee(sf2);
        pay2.setPaymentDate(LocalDateTime.now().minusDays(2));
        pay2.setAmount(5000.0);
        pay2.setPaymentMethod("CASH");
        pay2.setReceiptNumber("REC202600102");
        pay2.setReceivedBy("Accounts Office");
        feePaymentRepository.save(pay2);

        // 14. Notices & Events
        Notice n1 = new Notice();
        n1.setTitle("Annual Science Exhibition 2026-27");
        n1.setContent("All students of Classes 9 and 10 are invited to submit their science project models before the end of this month.");
        n1.setAudience(NoticeAudience.ALL);
        n1.setPublishedDate(today.minusDays(3));
        n1.setCreatedByUser(adminUser);
        noticeRepository.save(n1);

        Notice n2 = new Notice();
        n2.setTitle("Class 10 Mathematics Extra Practice Session");
        n2.setContent("Special practice session on coordinate geometry will be held this Saturday from 10:00 AM to 12:00 PM.");
        n2.setAudience(NoticeAudience.SPECIFIC_CLASS);
        n2.setSection(sec10A);
        n2.setPublishedDate(today.minusDays(1));
        n2.setCreatedByUser(teacherUser1);
        noticeRepository.save(n2);

        eventRepository.save(new Event("Independence Day Celebration", "Flag hoisting ceremony and cultural events.", EventType.EVENT, today.plusDays(5), today.plusDays(5)));
        eventRepository.save(new Event("Mid-Term Examinations", "Mid-term comprehensive assessments.", EventType.EXAM, today.plusDays(25), today.plusDays(35)));
        eventRepository.save(new Event("Parent Teacher Meeting (PTM)", "One-on-one progress review for Term 1.", EventType.PARENT_MEETING, today.plusDays(40), today.plusDays(40)));

        logger.info("Successfully seeded SmartSchool platform data!");
        logger.info("==========================================================================");
        logger.info("Credentials for instant testing:");
        logger.info("   ADMIN   : Username: admin          | Password: Admin@123");
        logger.info("   TEACHER : Username: T001           | Password: Teacher@123 (Ravi Sharma)");
        logger.info("   TEACHER : Username: T002           | Password: Teacher@123 (Sunita Verma)");
        logger.info("   STUDENT : Username: STU20260001    | Password: Student@123 (Rahul Sharma)");
        logger.info("   PARENT  : Username: PAR9876543210  | Password: Parent@123  (Rajesh Sharma)");
        logger.info("==========================================================================");
    }

    private Student createSampleStudent(String studentId, String admNo, String name, Section section, int roll, String gender, String blood, String phone, String password) {
        User u = new User(studentId, passwordEncoder.encode(password), RoleType.ROLE_STUDENT);
        u.setFirstLogin(false);
        u = userRepository.save(u);

        Student s = new Student();
        s.setUser(u);
        s.setStudentId(studentId);
        s.setAdmissionNumber(admNo);
        s.setName(name);
        s.setSection(section);
        s.setRollNumber(roll);
        s.setGender(gender);
        s.setBloodGroup(blood);
        s.setPhone(phone);
        s.setEmail(name.toLowerCase().replace(" ", ".") + "@example.com");
        s.setAddress("City Center School Area, District 1");
        s.setAdmissionDate(LocalDate.of(2024, 4, 1));
        s.setDateOfBirth(LocalDate.of(2010, 5, 15));
        s.setStatus(StudentStatus.ACTIVE);
        return studentRepository.save(s);
    }

    private void createTimetableSlot(Section sec, Subject sub, Teacher teacher, DayOfWeek dow, LocalTime start, LocalTime end, String room) {
        Timetable t = new Timetable();
        t.setSection(sec);
        t.setSubject(sub);
        t.setTeacher(teacher);
        t.setDayOfWeek(dow);
        t.setStartTime(start);
        t.setEndTime(end);
        t.setRoomNumber(room);
        timetableRepository.save(t);
    }
}
