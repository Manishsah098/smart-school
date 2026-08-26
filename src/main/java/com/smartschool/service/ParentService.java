package com.smartschool.service;

import com.smartschool.dto.ParentDashboardDTO;
import com.smartschool.dto.StudentDashboardDTO;
import com.smartschool.dto.StudentResponseDTO;
import com.smartschool.entity.Parent;
import com.smartschool.entity.ParentStudent;
import com.smartschool.entity.Student;
import com.smartschool.exception.ResourceNotFoundException;
import com.smartschool.exception.UnauthorizedAccessException;
import com.smartschool.repository.ParentRepository;
import com.smartschool.repository.ParentStudentRepository;
import com.smartschool.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ParentService {

    private final ParentRepository parentRepository;
    private final ParentStudentRepository parentStudentRepository;
    private final StudentRepository studentRepository;
    private final StudentService studentService;
    private final AdminService adminService;

    public ParentService(ParentRepository parentRepository,
                         ParentStudentRepository parentStudentRepository,
                         StudentRepository studentRepository,
                         StudentService studentService,
                         AdminService adminService) {
        this.parentRepository = parentRepository;
        this.parentStudentRepository = parentStudentRepository;
        this.studentRepository = studentRepository;
        this.studentService = studentService;
        this.adminService = adminService;
    }

    @Transactional(readOnly = true)
    public Parent getParentByUserId(Long userId) {
        return parentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent profile not found for user ID: " + userId));
    }

    @Transactional(readOnly = true)
    public ParentDashboardDTO getDashboard(Long parentUserId, Long selectedChildId) {
        Parent parent = getParentByUserId(parentUserId);
        List<ParentStudent> links = parentStudentRepository.findByParentId(parent.getId());
        List<StudentResponseDTO> children = links.stream()
                .map(link -> adminService.convertStudentToDTO(link.getStudent()))
                .collect(Collectors.toList());

        ParentDashboardDTO dto = new ParentDashboardDTO();
        dto.setParentId(parent.getId());
        dto.setParentName(parent.getName());
        dto.setChildren(children);

        if (!children.isEmpty()) {
            Long activeId = selectedChildId != null ? selectedChildId : children.get(0).getId();
            // Verify child belongs to parent
            boolean authorized = children.stream().anyMatch(c -> c.getId().equals(activeId));
            if (!authorized) {
                throw new UnauthorizedAccessException("You are not linked to this student");
            }

            Student activeStudent = studentRepository.findById(activeId).orElse(null);
            if (activeStudent != null) {
                dto.setActiveChildId(activeId);
                StudentDashboardDTO childDash = studentService.getDashboard(activeStudent.getUser().getId());
                dto.setActiveChildDashboard(childDash);
            }
        }

        return dto;
    }

    @Transactional(readOnly = true)
    public List<StudentResponseDTO> getChildren(Long parentUserId) {
        Parent parent = getParentByUserId(parentUserId);
        return parentStudentRepository.findByParentId(parent.getId()).stream()
                .map(link -> adminService.convertStudentToDTO(link.getStudent()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public void verifyChildOwnership(Long parentUserId, Long childStudentId) {
        Parent parent = getParentByUserId(parentUserId);
        if (!parentStudentRepository.existsByParentIdAndStudentId(parent.getId(), childStudentId)) {
            throw new UnauthorizedAccessException("You are not linked to this student record");
        }
    }
}
