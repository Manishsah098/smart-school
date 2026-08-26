package com.smartschool.dto;

import java.util.List;

public class ClassResponseDTO {
    private Long id;
    private String className;
    private Long academicYearId;
    private String academicYearName;
    private List<SectionResponseDTO> sections;

    public ClassResponseDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public Long getAcademicYearId() { return academicYearId; }
    public void setAcademicYearId(Long academicYearId) { this.academicYearId = academicYearId; }

    public String getAcademicYearName() { return academicYearName; }
    public void setAcademicYearName(String academicYearName) { this.academicYearName = academicYearName; }

    public List<SectionResponseDTO> getSections() { return sections; }
    public void setSections(List<SectionResponseDTO> sections) { this.sections = sections; }
}
