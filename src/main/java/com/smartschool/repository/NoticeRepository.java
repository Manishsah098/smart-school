package com.smartschool.repository;

import com.smartschool.entity.Notice;
import com.smartschool.entity.enums.NoticeAudience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {
    List<Notice> findAllByOrderByPublishedDateDesc();
    
    @Query("SELECT n FROM Notice n WHERE n.audience = 'ALL' OR n.audience = :audience OR (n.audience = 'SPECIFIC_CLASS' AND n.section.id = :sectionId) ORDER BY n.publishedDate DESC")
    List<Notice> findNoticesForSectionAndAudience(@Param("sectionId") Long sectionId, @Param("audience") NoticeAudience audience);

    @Query("SELECT n FROM Notice n WHERE n.audience = 'ALL' OR n.audience = :audience ORDER BY n.publishedDate DESC")
    List<Notice> findNoticesForAudience(@Param("audience") NoticeAudience audience);
}
