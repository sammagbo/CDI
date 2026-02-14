package com.school.cdi.controller;

import com.school.cdi.model.AttendanceLog;
import com.school.cdi.model.Student;
import com.school.cdi.repository.AttendanceLogRepository;
import com.school.cdi.repository.StudentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(originPatterns = "*") // Critical: Allow access from local HTML file
public class AttendanceController {

    private final StudentRepository studentRepository;
    private final AttendanceLogRepository attendanceLogRepository;

    // Constructor Injection
    public AttendanceController(StudentRepository studentRepository, AttendanceLogRepository attendanceLogRepository) {
        this.studentRepository = studentRepository;
        this.attendanceLogRepository = attendanceLogRepository;
    }

    @GetMapping("/health")
    public ResponseEntity<java.util.Map<String, String>> health() {
        return ResponseEntity.ok(java.util.Map.of("status", "UP"));
    }

    @GetMapping("/students")
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @PostMapping("/students")
    public ResponseEntity<Student> addStudent(@RequestBody Student student) {
        if (studentRepository.existsById(student.getId())) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(studentRepository.save(student));
    }

    @GetMapping("/students/present")
    public List<Student> getPresentStudents() {
        return studentRepository.findByIsPresentTrue();
    }

    @GetMapping("/logs")
    public List<AttendanceLog> getAllLogs() {
        return attendanceLogRepository.findAll();
    }

    @PostMapping("/logs")
    public ResponseEntity<AttendanceLog> addLog(@RequestBody AttendanceLog log) {
        return ResponseEntity.ok(attendanceLogRepository.save(log));
    }

    @PostMapping("/logs/batch")
    public ResponseEntity<java.util.Map<String, Integer>> addLogsBatch(@RequestBody List<AttendanceLog> logs) {
        List<AttendanceLog> saved = attendanceLogRepository.saveAll(logs);
        return ResponseEntity.ok(java.util.Map.of("inserted", saved.size()));
    }

    @DeleteMapping("/students/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable String id) {
        if (!studentRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        studentRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/students/{id}")
    public ResponseEntity<Student> updateStudent(@PathVariable String id, @RequestBody Student student) {
        if (!studentRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        student.setId(id); // Ensure ID matches path
        return ResponseEntity.ok(studentRepository.save(student));
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

        // Log the action
        AttendanceLog log = new AttendanceLog();
        log.setStudentId(student.getId());
        log.setAction(newStatus ? "IN" : "OUT");
        log.setTimestamp(LocalDateTime.now());
        attendanceLogRepository.save(log);

        return ResponseEntity.ok(updatedStudent);
    }
}
