package NTSA.Mukti_app.service;

import NTSA.Mukti_app.model.User;
import NTSA.Mukti_app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    // Save New User
    public User register(User user) {
        // Check if email already exists
        if (user.getEmail() != null && userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }
        // Check if phone already exists
        if (user.getPhone() != null && userRepository.findByPhone(user.getPhone()).isPresent()) {
            throw new IllegalArgumentException("Phone number already registered");
        }
        return userRepository.save(user);
    }

    // Login Logic
    public User login(String identifier, String password) {
        // 1. Check by Email
        User user = userRepository.findByEmail(identifier)
                // 2. If not found, Check by Phone
                .orElseGet(() -> userRepository.findByPhone(identifier).orElse(null));

        // 3. Check Password
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }
}