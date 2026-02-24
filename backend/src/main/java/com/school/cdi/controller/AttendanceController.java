package com.school.cdi.controller;

import com.school.cdi.model.AttendanceLog;
import com.school.cdi.model.Student;
import com.school.cdi.repository.AttendanceLogRepository;
import com.school.cdi.repository.StudentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(originPatterns = "*") // Critical: Allow access from local HTML file
public class AttendanceController {

    private final StudentRepository studentRepository;
    private final AttendanceLogRepository attendanceLogRepository;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
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
    public ResponseEntity<Map<String, Integer>> addLogsBatch(@RequestBody List<AttendanceLog> logs) {
        List<AttendanceLog> saved = attendanceLogRepository.saveAll(logs);
        return ResponseEntity.ok(Map.of("inserted", saved.size()));
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
