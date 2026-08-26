package com.smartschool.dto;

import com.smartschool.entity.enums.NoticeAudience;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class NoticeResponseDTO {
    private Long id;
    private String title;
    private String content;
    private NoticeAudience audience;
    private Long sectionId;
    private String sectionFullName;
    private LocalDate publishedDate;
    private LocalDate expiryDate;
    private String attachmentUrl;
    private String authorName;
    private LocalDateTime createdAt;

    public NoticeResponseDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public NoticeAudience getAudience() { return audience; }
    public void setAudience(NoticeAudience audience) { this.audience = audience; }

    public Long getSectionId() { return sectionId; }
    public void setSectionId(Long sectionId) { this.sectionId = sectionId; }

    public String getSectionFullName() { return sectionFullName; }
    public void setSectionFullName(String sectionFullName) { this.sectionFullName = sectionFullName; }

    public LocalDate getPublishedDate() { return publishedDate; }
    public void setPublishedDate(LocalDate publishedDate) { this.publishedDate = publishedDate; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public String getAttachmentUrl() { return attachmentUrl; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
