package com.smartschool.dto;

import com.smartschool.entity.enums.HomeworkStatus;
import jakarta.validation.constraints.NotNull;

public class HomeworkGradeRequest {

    @NotNull(message = "Status is required")
    private HomeworkStatus status;

    private Double marks;
    private String feedback;

    public HomeworkGradeRequest() {}

    public HomeworkStatus getStatus() { return status; }
    public void setStatus(HomeworkStatus status) { this.status = status; }

    public Double getMarks() { return marks; }
    public void setMarks(Double marks) { this.marks = marks; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
}
