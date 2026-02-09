package com.school.cdi.repository;

import com.school.cdi.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StudentRepository extends JpaRepository<Student, String> {
    List<Student> findByIsPresentTrue();
}
