package com.smartschool.dto;

import com.smartschool.entity.enums.NoticeAudience;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public class NoticeCreateRequest {

    @NotBlank(message = "Notice title is required")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    private NoticeAudience audience = NoticeAudience.ALL;
    private Long sectionId;
    private LocalDate publishedDate;
    private LocalDate expiryDate;
    private String attachmentUrl;

    public NoticeCreateRequest() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public NoticeAudience getAudience() { return audience; }
    public void setAudience(NoticeAudience audience) { this.audience = audience; }

    public Long getSectionId() { return sectionId; }
    public void setSectionId(Long sectionId) { this.sectionId = sectionId; }

    public LocalDate getPublishedDate() { return publishedDate; }
    public void setPublishedDate(LocalDate publishedDate) { this.publishedDate = publishedDate; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public String getAttachmentUrl() { return attachmentUrl; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }
}
