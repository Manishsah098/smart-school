package com.smartschool.dto;

import java.util.List;

public class StudentResultCardDTO {
    private Long examId;
    private String examName;
    private Long studentId;
    private String studentName;
    private String admissionNumber;
    private String classSectionName;
    private Integer rollNumber;
    private List<SubjectResultItem> subjects;
    private Double totalMarksObtained;
    private Double totalMaxMarks;
    private Double percentage;
    private String overallGrade;
    private String resultStatus; // PASS / FAIL

    public static class SubjectResultItem {
        private String subjectName;
        private Double maxMarks;
        private Double passingMarks;
        private Double marksObtained;
        private String grade;
        private String remarks;

        public SubjectResultItem() {}

        public SubjectResultItem(String subjectName, Double maxMarks, Double passingMarks, Double marksObtained, String grade, String remarks) {
            this.subjectName = subjectName;
            this.maxMarks = maxMarks;
            this.passingMarks = passingMarks;
            this.marksObtained = marksObtained;
            this.grade = grade;
            this.remarks = remarks;
        }

        public String getSubjectName() { return subjectName; }
        public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

        public Double getMaxMarks() { return maxMarks; }
        public void setMaxMarks(Double maxMarks) { this.maxMarks = maxMarks; }

        public Double getPassingMarks() { return passingMarks; }
        public void setPassingMarks(Double passingMarks) { this.passingMarks = passingMarks; }

        public Double getMarksObtained() { return marksObtained; }
        public void setMarksObtained(Double marksObtained) { this.marksObtained = marksObtained; }

        public String getGrade() { return grade; }
        public void setGrade(String grade) { this.grade = grade; }

        public String getRemarks() { return remarks; }
        public void setRemarks(String remarks) { this.remarks = remarks; }
    }

    public StudentResultCardDTO() {}

    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }

    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getAdmissionNumber() { return admissionNumber; }
    public void setAdmissionNumber(String admissionNumber) { this.admissionNumber = admissionNumber; }

    public String getClassSectionName() { return classSectionName; }
    public void setClassSectionName(String classSectionName) { this.classSectionName = classSectionName; }

    public Integer getRollNumber() { return rollNumber; }
    public void setRollNumber(Integer rollNumber) { this.rollNumber = rollNumber; }

    public List<SubjectResultItem> getSubjects() { return subjects; }
    public void setSubjects(List<SubjectResultItem> subjects) { this.subjects = subjects; }

    public Double getTotalMarksObtained() { return totalMarksObtained; }
    public void setTotalMarksObtained(Double totalMarksObtained) { this.totalMarksObtained = totalMarksObtained; }

    public Double getTotalMaxMarks() { return totalMaxMarks; }
    public void setTotalMaxMarks(Double totalMaxMarks) { this.totalMaxMarks = totalMaxMarks; }

    public Double getPercentage() { return percentage; }
    public void setPercentage(Double percentage) { this.percentage = percentage; }

    public String getOverallGrade() { return overallGrade; }
    public void setOverallGrade(String overallGrade) { this.overallGrade = overallGrade; }

    public String getResultStatus() { return resultStatus; }
    public void setResultStatus(String resultStatus) { this.resultStatus = resultStatus; }
}
