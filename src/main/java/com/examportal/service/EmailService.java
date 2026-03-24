package com.examportal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendRegistrationEmail(String to, String fullName, String username) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Welcome to ExamPortal!");
        message.setText(
            "Dear " + fullName + ",\n\n" +
            "Your account has been successfully created.\n" +
            "Username: " + username + "\n\n" +
            "You can now log in and start taking exams.\n\n" +
            "Best regards,\nExamPortal Team"
        );
        mailSender.send(message);
    }

    @Async
    public void sendResultEmail(String to, String fullName, String examTitle, int score, int total, boolean passed) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Your Exam Result - " + examTitle);
        message.setText(
            "Dear " + fullName + ",\n\n" +
            "Your result for \"" + examTitle + "\" is ready.\n\n" +
            "Score: " + score + " / " + total + "\n" +
            "Status: " + (passed ? "✅ PASSED" : "❌ FAILED") + "\n\n" +
            "Log in to ExamPortal to view your detailed result and download your PDF certificate.\n\n" +
            "Best regards,\nExamPortal Team"
        );
        mailSender.send(message);
    }
}
