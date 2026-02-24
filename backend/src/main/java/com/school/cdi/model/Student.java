package com.school.cdi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Student {

    @Id
    private String id; // Using String for UUID to match frontend ID format easily

    private String firstName;
    private String lastName;
    private String studentClass;

    private boolean isPresent;
    private LocalDateTime lastEntry;
}
