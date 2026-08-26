package com.smartschool.security;

import com.smartschool.entity.Parent;
import com.smartschool.entity.Student;
import com.smartschool.entity.Teacher;
import com.smartschool.repository.ParentRepository;
import com.smartschool.repository.ParentStudentRepository;
import com.smartschool.repository.StudentRepository;
import com.smartschool.repository.TeacherAssignmentRepository;
import com.smartschool.repository.TeacherRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("ownershipSecurityEvaluator")
public class OwnershipSecurityEvaluator {

    private final TeacherRepository teacherRepository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    private final ParentStudentRepository parentStudentRepository;

    public OwnershipSecurityEvaluator(TeacherRepository teacherRepository,
                                      TeacherAssignmentRepository teacherAssignmentRepository,
                                      StudentRepository studentRepository,
                                      ParentRepository parentRepository,
                                      ParentStudentRepository parentStudentRepository) {
        this.teacherRepository = teacherRepository;
        this.teacherAssignmentRepository = teacherAssignmentRepository;
        this.studentRepository = studentRepository;
        this.parentRepository = parentRepository;
        this.parentStudentRepository = parentStudentRepository;
    }

    public boolean canTeacherAccessSection(Authentication authentication, Long sectionId) {
        if (authentication == null || !authentication.isAuthenticated()) return false;
        if (hasRole(authentication, "ROLE_ADMIN")) return true;

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        Optional<Teacher> teacherOpt = teacherRepository.findByUserId(principal.getId());
        if (teacherOpt.isEmpty()) return false;

        return teacherAssignmentRepository.existsByTeacherIdAndSectionId(teacherOpt.get().getId(), sectionId);
    }

    public boolean canTeacherAccessStudent(Authentication authentication, Long studentId) {
        if (authentication == null || !authentication.isAuthenticated()) return false;
        if (hasRole(authentication, "ROLE_ADMIN")) return true;

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        Optional<Teacher> teacherOpt = teacherRepository.findByUserId(principal.getId());
        if (teacherOpt.isEmpty()) return false;

        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (studentOpt.isEmpty()) return false;

        Long sectionId = studentOpt.get().getSection().getId();
        return teacherAssignmentRepository.existsByTeacherIdAndSectionId(teacherOpt.get().getId(), sectionId);
    }

    public boolean canParentAccessStudent(Authentication authentication, Long studentId) {
        if (authentication == null || !authentication.isAuthenticated()) return false;
        if (hasRole(authentication, "ROLE_ADMIN")) return true;

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        Optional<Parent> parentOpt = parentRepository.findByUserId(principal.getId());
        if (parentOpt.isEmpty()) return false;

        return parentStudentRepository.existsByParentIdAndStudentId(parentOpt.get().getId(), studentId);
    }

    public boolean isStudentSelf(Authentication authentication, Long studentId) {
        if (authentication == null || !authentication.isAuthenticated()) return false;
        if (hasRole(authentication, "ROLE_ADMIN")) return true;

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        Optional<Student> studentOpt = studentRepository.findById(studentId);
        return studentOpt.isPresent() && studentOpt.get().getUser().getId().equals(principal.getId());
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }
}
