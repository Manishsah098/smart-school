package com.smartschool.dto;

import jakarta.validation.constraints.NotBlank;

public class HomeworkSubmissionRequest {

    @NotBlank(message = "Content or submission details are required")
    private String content;

    private String attachmentUrl;

    public HomeworkSubmissionRequest() {}

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getAttachmentUrl() { return attachmentUrl; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }
}
