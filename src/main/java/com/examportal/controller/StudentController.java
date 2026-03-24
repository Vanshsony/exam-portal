package com.examportal.controller;

import com.examportal.entity.*;
import com.examportal.service.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Map;

@Controller
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentController {

    private final ExamService examService;
    private final UserService userService;
    private final PdfService pdfService;

    private User getCurrentUser(UserDetails userDetails) {
        return userService.findByUsername(userDetails.getUsername());
    }

    // ---- Dashboard ----
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User student = getCurrentUser(userDetails);
        model.addAttribute("student", student);
        model.addAttribute("activeExams", examService.getActiveExams());
        model.addAttribute("results", examService.getStudentResults(student));
        return "student/dashboard";
    }

    // ---- Exam Taking ----
    @GetMapping("/exam/{examId}/start")
    public String startExam(@PathVariable Long examId,
                            @AuthenticationPrincipal UserDetails userDetails,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        try {
            User student = getCurrentUser(userDetails);
            ExamAttempt attempt = examService.startExam(student, examId);
            session.setAttribute("attemptId", attempt.getId());
            return "redirect:/student/exam/" + examId + "/take";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/student/dashboard";
        }
    }

    @GetMapping("/exam/{examId}/take")
    public String takeExam(@PathVariable Long examId,
                           @AuthenticationPrincipal UserDetails userDetails,
                           HttpSession session,
                           Model model) {
        Long attemptId = (Long) session.getAttribute("attemptId");
        if (attemptId == null) return "redirect:/student/exam/" + examId + "/start";

        ExamAttempt attempt = examService.getAttemptById(attemptId);
        if (attempt.getStatus() == ExamAttempt.AttemptStatus.COMPLETED) {
            return "redirect:/student/result/" + attemptId;
        }

        model.addAttribute("exam", examService.getExamById(examId));
        model.addAttribute("questions", examService.getQuestions(examId));
        model.addAttribute("attemptId", attemptId);
        model.addAttribute("durationSeconds", attempt.getExam().getDurationMinutes() * 60);
        return "student/take-exam";
    }

    @PostMapping("/exam/submit")
    public String submitExam(@RequestParam Long attemptId,
                             @RequestParam Map<String, String> allParams,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        try {
            allParams.remove("attemptId");
            examService.submitExam(attemptId, allParams);
            session.removeAttribute("attemptId");
            return "redirect:/student/result/" + attemptId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Submission failed: " + e.getMessage());
            return "redirect:/student/dashboard";
        }
    }

    // ---- Anti-cheat tab switch ----
    @PostMapping("/exam/tab-switch")
    @ResponseBody
    public ResponseEntity<String> recordTabSwitch(@RequestParam Long attemptId) {
        examService.recordTabSwitch(attemptId);
        ExamAttempt attempt = examService.getAttemptById(attemptId);
        if (attempt.getStatus() == ExamAttempt.AttemptStatus.FLAGGED) {
            return ResponseEntity.ok("FLAGGED");
        }
        return ResponseEntity.ok("count:" + attempt.getTabSwitchCount());
    }

    // ---- Results ----
    @GetMapping("/result/{attemptId}")
    public String viewResult(@PathVariable Long attemptId,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model) throws Exception {
        ExamAttempt attempt = examService.getAttemptById(attemptId);
        Map<String, String> answers = examService.getAnswersMap(attempt);
        model.addAttribute("attempt", attempt);
        model.addAttribute("questions", examService.getQuestions(attempt.getExam().getId()));
        model.addAttribute("answers", answers);
        return "student/result";
    }

    @GetMapping("/result/{attemptId}/pdf")
    public ResponseEntity<byte[]> downloadResultPdf(@PathVariable Long attemptId) throws Exception {
        ExamAttempt attempt = examService.getAttemptById(attemptId);
        byte[] pdf = pdfService.generateResultPdf(attempt);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=result-" + attemptId + ".pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdf);
    }

    // ---- Leaderboard ----
    @GetMapping("/leaderboard")
    public String leaderboard(Model model) {
        model.addAttribute("attempts", examService.getLeaderboard());
        model.addAttribute("exams", examService.getActiveExams());
        return "student/leaderboard";
    }
}
