package com.smartschool;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartschool.dto.LoginRequest;
import com.smartschool.entity.*;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class TeacherAccessControlTest {

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

    private String teacher1Token;
    private Section sectionB;
    private Student studentB;

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
                new AcademicYear("TEST-2026-2027", LocalDate.now(), LocalDate.now().plusYears(1), true));

        SchoolClass sc = classRepository.save(new SchoolClass("Class 10", year));
        Section sectionA = sectionRepository.save(new Section(sc, "A", "101", 40, null));
        sectionB = sectionRepository.save(new Section(sc, "B", "102", 40, null));

        User u1 = userRepository.save(new User("T_TEST_1", passwordEncoder.encode("Pass@123"), RoleType.ROLE_TEACHER));
        Teacher t1 = new Teacher();
        t1.setUser(u1); t1.setEmployeeId("T_TEST_1"); t1.setName("Teacher One");
        t1 = teacherRepository.save(t1);
        teacherAssignmentRepository.save(new TeacherAssignment(t1, sectionA, null, true));

        User u2 = userRepository.save(new User("T_TEST_2", passwordEncoder.encode("Pass@123"), RoleType.ROLE_TEACHER));
        Teacher t2 = new Teacher();
        t2.setUser(u2); t2.setEmployeeId("T_TEST_2"); t2.setName("Teacher Two");
        t2 = teacherRepository.save(t2);
        teacherAssignmentRepository.save(new TeacherAssignment(t2, sectionB, null, true));

        User sUser = userRepository.save(new User("STU_TEST_B", passwordEncoder.encode("Pass@123"), RoleType.ROLE_STUDENT));
        studentB = new Student();
        studentB.setUser(sUser); studentB.setStudentId("STU_TEST_B");
        studentB.setAdmissionNumber("ADM_TEST_B"); studentB.setName("Student in Sec B");
        studentB.setSection(sectionB);
        studentB = studentRepository.save(studentB);

        LoginRequest login = new LoginRequest("T_TEST_1", "Pass@123");
        MvcResult res = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk()).andReturn();

        teacher1Token = objectMapper.readTree(res.getResponse().getContentAsString())
                .get("data").get("token").asText();
    }

    @Test
    public void testTeacherCannotAccessUnauthorizedSectionStudents() throws Exception {
        mockMvc.perform(get("/api/teacher/classes/" + sectionB.getId() + "/students")
                .header("Authorization", "Bearer " + teacher1Token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    public void testTeacherCannotAccessUnauthorizedStudentProfile() throws Exception {
        mockMvc.perform(get("/api/teacher/students/" + studentB.getId())
                .header("Authorization", "Bearer " + teacher1Token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }
}
