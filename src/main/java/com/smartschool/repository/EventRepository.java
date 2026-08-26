package com.smartschool.repository;

import com.smartschool.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByStartDateGreaterThanEqualOrderByStartDateAsc(LocalDate date);
    List<Event> findAllByOrderByStartDateAsc();
}
