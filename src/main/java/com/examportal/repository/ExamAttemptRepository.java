package com.examportal.repository;

import com.examportal.entity.ExamAttempt;
import com.examportal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface ExamAttemptRepository extends JpaRepository<ExamAttempt, Long> {
    List<ExamAttempt> findByStudentOrderBySubmittedAtDesc(User student);
    List<ExamAttempt> findByExamIdOrderByScoreDesc(Long examId);
    Optional<ExamAttempt> findByStudentIdAndExamIdAndStatus(Long studentId, Long examId, ExamAttempt.AttemptStatus status);
    boolean existsByStudentIdAndExamIdAndStatus(Long studentId, Long examId, ExamAttempt.AttemptStatus status);
    long countByStatus(ExamAttempt.AttemptStatus status);

    @Query("SELECT a FROM ExamAttempt a WHERE a.status = 'COMPLETED' ORDER BY a.score DESC")
    List<ExamAttempt> findLeaderboard();
}
