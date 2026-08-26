package com.smartschool.service;

import com.smartschool.dto.TimetableEntryDTO;
import com.smartschool.entity.*;
import com.smartschool.exception.ResourceNotFoundException;
import com.smartschool.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TimetableService {

    private final TimetableRepository timetableRepository;
    private final SectionRepository sectionRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final AuditService auditService;

    public TimetableService(TimetableRepository timetableRepository,
                            SectionRepository sectionRepository,
                            SubjectRepository subjectRepository,
                            TeacherRepository teacherRepository,
                            StudentRepository studentRepository,
                            AuditService auditService) {
        this.timetableRepository = timetableRepository;
        this.sectionRepository = sectionRepository;
        this.subjectRepository = subjectRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
        this.auditService = auditService;
    }

    @Transactional
    public TimetableEntryDTO createEntry(TimetableEntryDTO dto, Long adminUserId, String ipAddress) {
        Section section = sectionRepository.findById(dto.getSectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Section not found"));
        Subject subject = subjectRepository.findById(dto.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
        Teacher teacher = teacherRepository.findById(dto.getTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));

        Timetable entry = new Timetable();
        entry.setSection(section);
        entry.setSubject(subject);
        entry.setTeacher(teacher);
        entry.setDayOfWeek(dto.getDayOfWeek());
        entry.setStartTime(dto.getStartTime());
        entry.setEndTime(dto.getEndTime());
        entry.setRoomNumber(dto.getRoomNumber());

        entry = timetableRepository.save(entry);

        auditService.log(adminUserId, "admin", "CREATE_TIMETABLE", "Timetable", entry.getId(),
                "Created timetable entry for " + section.getFullName() + " (" + dto.getDayOfWeek() + ")", ipAddress);

        return convertToDTO(entry);
    }

    @Transactional(readOnly = true)
    public List<TimetableEntryDTO> getTimetableForSection(Long sectionId) {
        return timetableRepository.findBySectionIdOrderByDayOfWeekAscStartTimeAsc(sectionId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TimetableEntryDTO> getTimetableForTeacher(Long teacherUserId) {
        Teacher teacher = teacherRepository.findByUserId(teacherUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));

        return timetableRepository.findByTeacherIdOrderByDayOfWeekAscStartTimeAsc(teacher.getId()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TimetableEntryDTO> getTimetableForStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        return getTimetableForSection(student.getSection().getId());
    }

    public TimetableEntryDTO convertToDTO(Timetable t) {
        TimetableEntryDTO dto = new TimetableEntryDTO();
        dto.setId(t.getId());
        dto.setSectionId(t.getSection().getId());
        dto.setSectionFullName(t.getSection().getFullName());
        dto.setSubjectId(t.getSubject().getId());
        dto.setSubjectName(t.getSubject().getSubjectName());
        dto.setTeacherId(t.getTeacher().getId());
        dto.setTeacherName(t.getTeacher().getName());
        dto.setDayOfWeek(t.getDayOfWeek());
        dto.setStartTime(t.getStartTime());
        dto.setEndTime(t.getEndTime());
        dto.setRoomNumber(t.getRoomNumber());
        return dto;
    }
}
