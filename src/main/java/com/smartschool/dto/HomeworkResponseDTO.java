package com.smartschool.dto;

import com.smartschool.entity.enums.HomeworkStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class HomeworkResponseDTO {
    private Long id;
    private Long sectionId;
    private String sectionFullName;
    private Long subjectId;
    private String subjectName;
    private Long teacherId;
    private String teacherName;
    private String title;
    private String description;
    private LocalDate assignedDate;
    private LocalDate dueDate;
    private String attachmentUrl;
    
    // For student view
    private HomeworkStatus submissionStatus;
    private LocalDateTime submittedAt;
    private String studentContent;
    private String studentAttachmentUrl;
    private String feedback;
    private Double marks;

    public HomeworkResponseDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSectionId() { return sectionId; }
    public void setSectionId(Long sectionId) { this.sectionId = sectionId; }

    public String getSectionFullName() { return sectionFullName; }
    public void setSectionFullName(String sectionFullName) { this.sectionFullName = sectionFullName; }

    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getAssignedDate() { return assignedDate; }
    public void setAssignedDate(LocalDate assignedDate) { this.assignedDate = assignedDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public String getAttachmentUrl() { return attachmentUrl; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }

    public HomeworkStatus getSubmissionStatus() { return submissionStatus; }
    public void setSubmissionStatus(HomeworkStatus submissionStatus) { this.submissionStatus = submissionStatus; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public String getStudentContent() { return studentContent; }
    public void setStudentContent(String studentContent) { this.studentContent = studentContent; }

    public String getStudentAttachmentUrl() { return studentAttachmentUrl; }
    public void setStudentAttachmentUrl(String studentAttachmentUrl) { this.studentAttachmentUrl = studentAttachmentUrl; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public Double getMarks() { return marks; }
    public void setMarks(Double marks) { this.marks = marks; }
}
