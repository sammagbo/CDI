package com.school.cdi.repository;

import com.school.cdi.model.AttendanceLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceLogRepository extends JpaRepository<AttendanceLog, Long> {
}
