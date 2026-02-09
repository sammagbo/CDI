package com.school.cdi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Student {

    @Id
    private String id; // Using String for UUID to match frontend ID format easily

    private String firstName;
    private String lastName;
    private String studentClass;

    private boolean isPresent;
    private LocalDateTime lastEntry;
}
