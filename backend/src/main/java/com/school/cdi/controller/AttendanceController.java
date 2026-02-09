package com.school.cdi.controller;

import com.school.cdi.model.Student;
import com.school.cdi.repository.StudentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Critical: Allow access from local HTML file
public class AttendanceController {

    private final StudentRepository studentRepository;

    // Constructor Injection
    public AttendanceController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @GetMapping("/students")
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @GetMapping("/students/present")
    public List<Student> getPresentStudents() {
        return studentRepository.findByIsPresentTrue();
    }

    @PostMapping("/scan/{studentId}")
    public ResponseEntity<Student> scanStudent(@PathVariable String studentId) {
        Optional<Student> studentOpt = studentRepository.findById(studentId);

        if (studentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Student student = studentOpt.get();
        // Toggle presence
        boolean newStatus = !student.isPresent();
        student.setPresent(newStatus);

        if (newStatus) {
            student.setLastEntry(LocalDateTime.now());
        }

        Student updatedStudent = studentRepository.save(student);
        return ResponseEntity.ok(updatedStudent);
    }
}
