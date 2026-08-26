package com.smartschool;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartschool.dto.*;
import com.smartschool.entity.*;
import com.smartschool.entity.enums.AttendanceStatus;
import com.smartschool.entity.enums.RoleType;
import com.smartschool.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class AttendanceAndMarksTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private TeacherRepository teacherRepository;
    @Autowired private AcademicYearRepository academicYearRepository;
    @Autowired private SchoolClassRepository classRepository;
    @Autowired private SectionRepository sectionRepository;
    @Autowired private TeacherAssignmentRepository teacherAssignmentRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private AttendanceRepository attendanceRepository;
    @Autowired private MarkRepository markRepository;
    @Autowired private HomeworkSubmissionRepository submissionRepository;
    @Autowired private HomeworkRepository homeworkRepository;
    @Autowired private StudentFeeRepository studentFeeRepository;
    @Autowired private FeePaymentRepository feePaymentRepository;
    @Autowired private ParentStudentRepository parentStudentRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private String teacherToken;
    private Section section;
    private Student student;

    @BeforeEach
    public void setup() throws Exception {
        feePaymentRepository.deleteAll();
        studentFeeRepository.deleteAll();
        submissionRepository.deleteAll();
        homeworkRepository.deleteAll();
        markRepository.deleteAll();
        attendanceRepository.deleteAll();
        parentStudentRepository.deleteAll();
        studentRepository.deleteAll();
        teacherAssignmentRepository.deleteAll();
        sectionRepository.deleteAll();
        classRepository.deleteAll();
        teacherRepository.deleteAll();
        userRepository.deleteAll();
        academicYearRepository.deleteAll();

        AcademicYear year = academicYearRepository.save(
                new AcademicYear("ATT-TEST-2026", LocalDate.now(), LocalDate.now().plusYears(1), true));
        SchoolClass sc = classRepository.save(new SchoolClass("Class 10", year));
        section = sectionRepository.save(new Section(sc, "A", "101", 40, null));

        User u = userRepository.save(new User("T_ATT_1", passwordEncoder.encode("Pass@123"), RoleType.ROLE_TEACHER));
        Teacher t = new Teacher();
        t.setUser(u); t.setEmployeeId("T_ATT_1"); t.setName("Teacher Attendance");
        t = teacherRepository.save(t);
        teacherAssignmentRepository.save(new TeacherAssignment(t, section, null, true));

        User sUser = userRepository.save(new User("STU_ATT_1", passwordEncoder.encode("Pass@123"), RoleType.ROLE_STUDENT));
        student = new Student();
        student.setUser(sUser); student.setStudentId("STU_ATT_1");
        student.setAdmissionNumber("ADM_ATT_1"); student.setName("Student Attendance Test");
        student.setSection(section);
        student = studentRepository.save(student);

        LoginRequest login = new LoginRequest("T_ATT_1", "Pass@123");
        MvcResult res = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk()).andReturn();

        teacherToken = objectMapper.readTree(res.getResponse().getContentAsString())
                .get("data").get("token").asText();
    }

    @Test
    public void testMarkBatchAttendance() throws Exception {
        AttendanceBatchRequest request = new AttendanceBatchRequest();
        request.setSectionId(section.getId());
        request.setDate(LocalDate.now());
        request.setItems(List.of(
                new AttendanceBatchRequest.StudentAttendanceItem(student.getId(), AttendanceStatus.PRESENT, "Present")));

        mockMvc.perform(post("/api/teacher/attendance")
                .header("Authorization", "Bearer " + teacherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].status").value("PRESENT"));
    }
}
