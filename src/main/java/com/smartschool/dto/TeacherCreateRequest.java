package com.smartschool.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.List;

public class TeacherCreateRequest {

    @NotBlank(message = "Teacher name is required")
    private String name;

    @NotBlank(message = "Employee ID is required")
    private String employeeId;

    @Email(message = "Valid email is required")
    private String email;

    private String phone;
    private String gender;
    private LocalDate dateOfBirth;
    private String address;
    private String qualification;
    private LocalDate joiningDate;
    private String department;

    // List of section IDs to assign
    private List<Long> sectionIds;
    // List of subject IDs to assign
    private List<Long> subjectIds;

    public TeacherCreateRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }

    public LocalDate getJoiningDate() { return joiningDate; }
    public void setJoiningDate(LocalDate joiningDate) { this.joiningDate = joiningDate; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public List<Long> getSectionIds() { return sectionIds; }
    public void setSectionIds(List<Long> sectionIds) { this.sectionIds = sectionIds; }

    public List<Long> getSubjectIds() { return subjectIds; }
    public void setSubjectIds(List<Long> subjectIds) { this.subjectIds = subjectIds; }
}
