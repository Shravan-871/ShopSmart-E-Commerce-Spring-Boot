package com.shopsmart.shopsmart;

import com.shopsmart.model.User;
import com.shopsmart.repository.UserRepository;
import com.shopsmart.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepo;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks UserService userService;

    @Test
    void usernameExistsDelegatesToRepository() {
        when(userRepo.existsByUsername("alice")).thenReturn(true);
        assertTrue(userService.usernameExists("alice"));
    }

    @Test
    void registerHashesPasswordAndSavesUser() {
        when(passwordEncoder.encode("secret12")).thenReturn("hashed");
        userService.register("bob", "secret12");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(captor.capture());
        User saved = captor.getValue();
        assertEquals("bob", saved.getUsername());
        assertEquals("hashed", saved.getPassword());
        assertEquals("USER", saved.getRole());
    }
}
