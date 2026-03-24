package com.examportal.controller;

import com.examportal.entity.*;
import com.examportal.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ExamService examService;
    private final UserService userService;

    // ---- Dashboard ----
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("stats", examService.getDashboardStats());
        model.addAttribute("recentExams", examService.getAllExams());
        model.addAttribute("studentCount", userService.countStudents());
        return "admin/dashboard";
    }

    // ---- Exam Management ----
    @GetMapping("/exams")
    public String listExams(Model model) {
        model.addAttribute("exams", examService.getAllExams());
        return "admin/exams";
    }

    @GetMapping("/exams/new")
    public String newExamForm() {
        return "admin/exam-form";
    }

    @PostMapping("/exams/new")
    public String createExam(@RequestParam String title,
                             @RequestParam String description,
                             @RequestParam int durationMinutes,
                             @RequestParam int totalMarks,
                             @RequestParam int passingMarks,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        User admin = userService.findByUsername(userDetails.getUsername());
        Exam exam = examService.createExam(title, description, durationMinutes, totalMarks, passingMarks, admin);
        redirectAttributes.addFlashAttribute("success", "Exam created! Now add questions.");
        return "redirect:/admin/exams/" + exam.getId() + "/questions";
    }

    @GetMapping("/exams/{id}/questions")
    public String manageQuestions(@PathVariable Long id, Model model) {
        model.addAttribute("exam", examService.getExamById(id));
        model.addAttribute("questions", examService.getQuestions(id));
        return "admin/questions";
    }

    @PostMapping("/exams/{id}/questions/add")
    public String addQuestion(@PathVariable Long id,
                              @RequestParam String questionText,
                              @RequestParam String optionA,
                              @RequestParam String optionB,
                              @RequestParam String optionC,
                              @RequestParam String optionD,
                              @RequestParam String correctAnswer,
                              @RequestParam(defaultValue = "1") int marks,
                              RedirectAttributes redirectAttributes) {
        examService.addQuestion(id, questionText, optionA, optionB, optionC, optionD, correctAnswer, marks);
        redirectAttributes.addFlashAttribute("success", "Question added.");
        return "redirect:/admin/exams/" + id + "/questions";
    }

    @PostMapping("/exams/questions/{qId}/delete")
    public String deleteQuestion(@PathVariable Long qId,
                                 @RequestParam Long examId,
                                 RedirectAttributes redirectAttributes) {
        examService.deleteQuestion(qId);
        redirectAttributes.addFlashAttribute("success", "Question deleted.");
        return "redirect:/admin/exams/" + examId + "/questions";
    }

    @PostMapping("/exams/{id}/toggle")
    public String toggleExam(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        examService.toggleExamStatus(id);
        redirectAttributes.addFlashAttribute("success", "Exam status updated.");
        return "redirect:/admin/exams";
    }

    @PostMapping("/exams/{id}/delete")
    public String deleteExam(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        examService.deleteExam(id);
        redirectAttributes.addFlashAttribute("success", "Exam deleted.");
        return "redirect:/admin/exams";
    }

    // ---- Leaderboard ----
    @GetMapping("/leaderboard")
    public String leaderboard(Model model) {
        model.addAttribute("attempts", examService.getLeaderboard());
        model.addAttribute("exams", examService.getAllExams());
        return "admin/leaderboard";
    }

    @GetMapping("/leaderboard/exam/{examId}")
    public String examLeaderboard(@PathVariable Long examId, Model model) {
        model.addAttribute("attempts", examService.getExamLeaderboard(examId));
        model.addAttribute("exam", examService.getExamById(examId));
        model.addAttribute("exams", examService.getAllExams());
        return "admin/leaderboard";
    }

    // ---- Student Management ----
    @GetMapping("/students")
    public String listStudents(Model model) {
        model.addAttribute("students", userService.getAllStudents());
        return "admin/students";
    }

    @PostMapping("/students/{id}/toggle")
    public String toggleStudent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.toggleUserStatus(id);
        redirectAttributes.addFlashAttribute("success", "Student status updated.");
        return "redirect:/admin/students";
    }
}
