package com.antivirus.server.service;

import com.antivirus.server.model.User;
import com.antivirus.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        Collection<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole())
        );

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getEnabled(),
                true, true, true,
                authorities
        );
    }

    private static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$";
    private static final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);

    public User registerUser(String username, String email, String password, String role, PasswordEncoder passwordEncoder) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Имя пользователя уже существует.");
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email уже существует.");
        }

        if (!isPasswordValid(password)) {
            throw new IllegalArgumentException(
                    "Пароль не соответствует требованиям безопасности. " +
                            "Пароль должен содержать не менее 8 символов, не менее одной цифры и специальный символ."
            );
        }

        if (role == null || (!role.equals("USER") && !role.equals("MANAGER") && !role.equals("ADMIN"))) {
            role = "USER";
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setEnabled(true);

        return userRepository.save(user);
    }

    private boolean isPasswordValid(String password) {
        return pattern.matcher(password).matches();
    }
}
