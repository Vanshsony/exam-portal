package com.examportal.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "exam_attempts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    private int score;
    private int totalMarks;
    private boolean passed;

    private int tabSwitchCount = 0;

    @Enumerated(EnumType.STRING)
    private AttemptStatus status;

    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;

    // Store answers as JSON string: {"questionId": "A", ...}
    @Column(columnDefinition = "TEXT")
    private String answersJson;

    public enum AttemptStatus {
        IN_PROGRESS, COMPLETED, FLAGGED
    }
}
