package com.smartschool.service;

import com.smartschool.dto.AuthResponse;
import com.smartschool.dto.ChangePasswordRequest;
import com.smartschool.dto.LoginRequest;
import com.smartschool.entity.Parent;
import com.smartschool.entity.Student;
import com.smartschool.entity.Teacher;
import com.smartschool.entity.User;
import com.smartschool.exception.BadRequestException;
import com.smartschool.exception.ResourceNotFoundException;
import com.smartschool.repository.ParentRepository;
import com.smartschool.repository.StudentRepository;
import com.smartschool.repository.TeacherRepository;
import com.smartschool.repository.UserRepository;
import com.smartschool.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider,
                       UserRepository userRepository,
                       TeacherRepository teacherRepository,
                       StudentRepository studentRepository,
                       ParentRepository parentRepository,
                       PasswordEncoder passwordEncoder,
                       AuditService auditService) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
        this.parentRepository = parentRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String token = tokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole().name(), user.isFirstLogin());

        String displayName = resolveDisplayName(user);

        auditService.log(user.getId(), user.getUsername(), "LOGIN", "User", user.getId(), "User logged in successfully", ipAddress);

        return new AuthResponse(token, user.getId(), user.getUsername(), displayName, user.getRole().name(), user.isFirstLogin());
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request, String ipAddress) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirm password do not match");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("New password cannot be the same as old password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setFirstLogin(false);
        userRepository.save(user);

        auditService.log(user.getId(), user.getUsername(), "CHANGE_PASSWORD", "User", user.getId(), "Password changed successfully", ipAddress);
    }

    private String resolveDisplayName(User user) {
        switch (user.getRole()) {
            case ROLE_ADMIN:
                return "School Administrator";
            case ROLE_TEACHER:
                return teacherRepository.findByUserId(user.getId()).map(Teacher::getName).orElse(user.getUsername());
            case ROLE_STUDENT:
                return studentRepository.findByUserId(user.getId()).map(Student::getName).orElse(user.getUsername());
            case ROLE_PARENT:
                return parentRepository.findByUserId(user.getId()).map(Parent::getName).orElse(user.getUsername());
            default:
                return user.getUsername();
        }
    }
}
