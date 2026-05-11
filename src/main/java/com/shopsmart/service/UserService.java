package com.shopsmart.service;

import com.shopsmart.model.User;
import com.shopsmart.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;

    public UserService(UserRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    public boolean usernameExists(String username) {
        return repo.existsByUsername(username);
    }

    public void register(String username, String rawPassword) {
        repo.save(new User(username, encoder.encode(rawPassword), "USER"));
    }
}
