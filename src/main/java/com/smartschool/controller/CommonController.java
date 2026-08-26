package com.smartschool.controller;

import com.smartschool.dto.ApiResponse;
import com.smartschool.dto.EventDTO;
import com.smartschool.dto.SectionResponseDTO;
import com.smartschool.dto.StudentResponseDTO;
import com.smartschool.dto.TeacherResponseDTO;
import com.smartschool.entity.Event;
import com.smartschool.entity.Student;
import com.smartschool.entity.Teacher;
import com.smartschool.entity.enums.RoleType;
import com.smartschool.repository.EventRepository;
import com.smartschool.repository.StudentRepository;
import com.smartschool.repository.TeacherRepository;
import com.smartschool.security.UserPrincipal;
import com.smartschool.service.AdminService;
import com.smartschool.service.TeacherService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/common")
public class CommonController {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final EventRepository eventRepository;
    private final AdminService adminService;
    private final TeacherService teacherService;

    public CommonController(StudentRepository studentRepository,
                            TeacherRepository teacherRepository,
                            EventRepository eventRepository,
                            AdminService adminService,
                            TeacherService teacherService) {
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.eventRepository = eventRepository;
        this.adminService = adminService;
        this.teacherService = teacherService;
    }

    @GetMapping("/events")
    public ResponseEntity<ApiResponse<List<EventDTO>>> getEvents() {
        List<EventDTO> events = eventRepository.findAllByOrderByStartDateAsc().stream()
                .map(e -> new EventDTO(e.getId(), e.getTitle(), e.getDescription(), e.getEventType(), e.getStartDate(), e.getEndDate()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Events retrieved", events));
    }

    @PostMapping("/events")
    public ResponseEntity<ApiResponse<Event>> createEvent(@RequestBody EventDTO dto) {
        Event event = new Event(dto.getTitle(), dto.getDescription(), dto.getEventType(), dto.getStartDate(), dto.getEndDate());
        event = eventRepository.save(event);
        return ResponseEntity.ok(ApiResponse.success("Event created successfully", event));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Map<String, Object>>> search(@RequestParam String q,
                                                                   @AuthenticationPrincipal UserPrincipal principal) {
        Map<String, Object> results = new HashMap<>();

        if (principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(RoleType.ROLE_ADMIN.name()))) {
            List<StudentResponseDTO> students = studentRepository.searchStudents(q).stream()
                    .map(adminService::convertStudentToDTO)
                    .collect(Collectors.toList());
            List<TeacherResponseDTO> teachers = teacherRepository.findAll().stream()
                    .filter(t -> t.getName().toLowerCase().contains(q.toLowerCase()) || t.getEmployeeId().toLowerCase().contains(q.toLowerCase()))
                    .map(adminService::convertTeacherToDTO)
                    .collect(Collectors.toList());
            results.put("students", students);
            results.put("teachers", teachers);
        } else if (principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(RoleType.ROLE_TEACHER.name()))) {
            List<SectionResponseDTO> myClasses = teacherService.getMyClasses(principal.getId());
            List<StudentResponseDTO> students = new ArrayList<>();
            for (SectionResponseDTO sec : myClasses) {
                List<Student> found = studentRepository.searchStudentsInSection(sec.getId(), q);
                for (Student s : found) {
                    students.add(adminService.convertStudentToDTO(s));
                }
            }
            results.put("students", students);
        }

        return ResponseEntity.ok(ApiResponse.success("Search results", results));
    }
}

