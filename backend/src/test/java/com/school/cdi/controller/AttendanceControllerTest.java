package com.school.cdi.controller;

import com.school.cdi.model.Student;
import com.school.cdi.repository.AttendanceLogRepository;
import com.school.cdi.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class AttendanceControllerTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private AttendanceLogRepository attendanceLogRepository;

    private AttendanceController attendanceController;

    @BeforeEach
    void setUp() {
        attendanceController = new AttendanceController(studentRepository, attendanceLogRepository);
    }

    @Test
    void addStudentShouldReturnBadRequestWhenIdIsNull() {
        Student student = new Student();
        student.setId(null);

        ResponseEntity<Student> response = attendanceController.addStudent(student);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verifyNoInteractions(studentRepository);
    }
}
