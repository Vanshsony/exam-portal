package com.examportal.service;

import com.examportal.entity.User;
import com.examportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public User registerStudent(String username, String password, String fullName, String email) {
        if (userRepository.existsByUsername(username)) throw new RuntimeException("Username already taken");
        if (userRepository.existsByEmail(email)) throw new RuntimeException("Email already registered");

        User user = User.builder()
            .username(username)
            .password(passwordEncoder.encode(password))
            .fullName(fullName)
            .email(email)
            .role(User.Role.STUDENT)
            .enabled(true)
            .build();

        User saved = userRepository.save(user);
        emailService.sendRegistrationEmail(email, fullName, username);
        return saved;
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<User> getAllStudents() {
        return userRepository.findAll().stream()
            .filter(u -> u.getRole() == User.Role.STUDENT)
            .toList();
    }

    public void toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
    }

    public long countStudents() {
        return userRepository.countByRole(User.Role.STUDENT);
    }
}
