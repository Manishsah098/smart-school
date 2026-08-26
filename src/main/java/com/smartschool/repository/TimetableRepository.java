package com.smartschool.repository;

import com.smartschool.entity.Timetable;
import com.smartschool.entity.enums.DayOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimetableRepository extends JpaRepository<Timetable, Long> {
    List<Timetable> findBySectionIdOrderByDayOfWeekAscStartTimeAsc(Long sectionId);
    List<Timetable> findByTeacherIdOrderByDayOfWeekAscStartTimeAsc(Long teacherId);
    List<Timetable> findBySectionIdAndDayOfWeekOrderByStartTimeAsc(Long sectionId, DayOfWeek dayOfWeek);
}
