package com.smartschool.entity;

import com.smartschool.entity.enums.HomeworkStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "homework_submissions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"homework_id", "student_id"})
})
public class HomeworkSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "homework_id", nullable = false)
    private Homework homework;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "submission_date")
    private LocalDateTime submissionDate;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "attachment_url", length = 255)
    private String attachmentUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private HomeworkStatus status = HomeworkStatus.PENDING;

    @Column(name = "feedback", length = 255)
    private String feedback;

    @Column(name = "marks")
    private Double marks;

    public HomeworkSubmission() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Homework getHomework() { return homework; }
    public void setHomework(Homework homework) { this.homework = homework; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public LocalDateTime getSubmissionDate() { return submissionDate; }
    public void setSubmissionDate(LocalDateTime submissionDate) { this.submissionDate = submissionDate; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getAttachmentUrl() { return attachmentUrl; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }

    public HomeworkStatus getStatus() { return status; }
    public void setStatus(HomeworkStatus status) { this.status = status; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public Double getMarks() { return marks; }
    public void setMarks(Double marks) { this.marks = marks; }
}
