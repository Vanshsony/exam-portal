package com.examportal.service;

import com.examportal.entity.*;
import com.examportal.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ExamService {

    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final ExamAttemptRepository attemptRepository;
    private final EmailService emailService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ---- Admin Operations ----

    public Exam createExam(String title, String description, int duration, int totalMarks, int passingMarks, User admin) {
        Exam exam = Exam.builder()
            .title(title).description(description)
            .durationMinutes(duration).totalMarks(totalMarks)
            .passingMarks(passingMarks).active(true).createdBy(admin)
            .build();
        return examRepository.save(exam);
    }

    public Question addQuestion(Long examId, String text, String a, String b, String c, String d, String correct, int marks) {
        Exam exam = examRepository.findById(examId).orElseThrow();
        Question q = Question.builder()
            .questionText(text).optionA(a).optionB(b).optionC(c).optionD(d)
            .correctAnswer(correct).marks(marks).exam(exam)
            .build();
        return questionRepository.save(q);
    }

    public void deleteQuestion(Long questionId) {
        questionRepository.deleteById(questionId);
    }

    public void toggleExamStatus(Long examId) {
        Exam exam = examRepository.findById(examId).orElseThrow();
        exam.setActive(!exam.isActive());
        examRepository.save(exam);
    }

    public void deleteExam(Long examId) {
        examRepository.deleteById(examId);
    }

    public List<Exam> getAllExams() { return examRepository.findAll(); }
    public List<Exam> getActiveExams() { return examRepository.findByActiveTrue(); }
    public Exam getExamById(Long id) { return examRepository.findById(id).orElseThrow(); }
    public List<Question> getQuestions(Long examId) { return questionRepository.findByExamId(examId); }

    // ---- Student Operations ----

    @Transactional
    public ExamAttempt startExam(User student, Long examId) {
        if (attemptRepository.existsByStudentIdAndExamIdAndStatus(student.getId(), examId, ExamAttempt.AttemptStatus.COMPLETED)) {
            throw new RuntimeException("You have already completed this exam.");
        }
        // Check for existing in-progress attempt
        Optional<ExamAttempt> existing = attemptRepository
            .findByStudentIdAndExamIdAndStatus(student.getId(), examId, ExamAttempt.AttemptStatus.IN_PROGRESS);
        if (existing.isPresent()) return existing.get();

        Exam exam = examRepository.findById(examId).orElseThrow();
        ExamAttempt attempt = ExamAttempt.builder()
            .student(student).exam(exam)
            .status(ExamAttempt.AttemptStatus.IN_PROGRESS)
            .startedAt(LocalDateTime.now())
            .totalMarks(exam.getTotalMarks())
            .build();
        return attemptRepository.save(attempt);
    }

    @Transactional
    public ExamAttempt submitExam(Long attemptId, Map<String, String> answers) throws Exception {
        ExamAttempt attempt = attemptRepository.findById(attemptId).orElseThrow();
        List<Question> questions = questionRepository.findByExamId(attempt.getExam().getId());

        int score = 0;
        for (Question q : questions) {
            String studentAnswer = answers.get(String.valueOf(q.getId()));
            if (q.getCorrectAnswer().equalsIgnoreCase(studentAnswer)) {
                score += q.getMarks();
            }
        }

        attempt.setScore(score);
        attempt.setPassed(score >= attempt.getExam().getPassingMarks());
        attempt.setStatus(ExamAttempt.AttemptStatus.COMPLETED);
        attempt.setSubmittedAt(LocalDateTime.now());
        attempt.setAnswersJson(objectMapper.writeValueAsString(answers));

        ExamAttempt saved = attemptRepository.save(attempt);

        // Send result email async
        emailService.sendResultEmail(
            attempt.getStudent().getEmail(),
            attempt.getStudent().getFullName(),
            attempt.getExam().getTitle(),
            score, attempt.getTotalMarks(),
            attempt.isPassed()
        );

        return saved;
    }

    @Transactional
    public void recordTabSwitch(Long attemptId) {
        attemptRepository.findById(attemptId).ifPresent(attempt -> {
            attempt.setTabSwitchCount(attempt.getTabSwitchCount() + 1);
            if (attempt.getTabSwitchCount() >= 3) {
                attempt.setStatus(ExamAttempt.AttemptStatus.FLAGGED);
            }
            attemptRepository.save(attempt);
        });
    }

    public List<ExamAttempt> getStudentResults(User student) {
        return attemptRepository.findByStudentOrderBySubmittedAtDesc(student);
    }

    public ExamAttempt getAttemptById(Long id) {
        return attemptRepository.findById(id).orElseThrow();
    }

    public List<ExamAttempt> getLeaderboard() {
        return attemptRepository.findLeaderboard();
    }

    public List<ExamAttempt> getExamLeaderboard(Long examId) {
        return attemptRepository.findByExamIdOrderByScoreDesc(examId);
    }

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalExams", examRepository.count());
        stats.put("activeExams", examRepository.countByActiveTrue());
        stats.put("totalAttempts", attemptRepository.count());
        stats.put("completedAttempts", attemptRepository.countByStatus(ExamAttempt.AttemptStatus.COMPLETED));
        return stats;
    }

    public Map<String, String> getAnswersMap(ExamAttempt attempt) throws Exception {
        if (attempt.getAnswersJson() == null) return new HashMap<>();
        return objectMapper.readValue(attempt.getAnswersJson(), new TypeReference<Map<String, String>>() {});
    }
}
