package com.smartschool.dto;

import java.util.List;

public class ParentDashboardDTO {
    private Long parentId;
    private String parentName;
    private List<StudentResponseDTO> children;
    private Long activeChildId;
    private StudentDashboardDTO activeChildDashboard;

    public ParentDashboardDTO() {}

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    public String getParentName() { return parentName; }
    public void setParentName(String parentName) { this.parentName = parentName; }

    public List<StudentResponseDTO> getChildren() { return children; }
    public void setChildren(List<StudentResponseDTO> children) { this.children = children; }

    public Long getActiveChildId() { return activeChildId; }
    public void setActiveChildId(Long activeChildId) { this.activeChildId = activeChildId; }

    public StudentDashboardDTO getActiveChildDashboard() { return activeChildDashboard; }
    public void setActiveChildDashboard(StudentDashboardDTO activeChildDashboard) { this.activeChildDashboard = activeChildDashboard; }
}
