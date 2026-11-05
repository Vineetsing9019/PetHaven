package com.itvedant.pethaven.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.itvedant.pethaven.models.User;
import com.itvedant.pethaven.models.UserDto;
import com.itvedant.pethaven.repositories.UserRepository;

// Unit tests for registration. The repository and password encoder are mocked.
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void register_newEmail_savesUserWithHashedPasswordAndUserRole() {
        UserDto dto = newUser();
        when(userRepository.findByEmail("asha@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret123")).thenReturn("hashed-secret");

        String message = userService.register(dto);

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertEquals("User registered successfully", message);
        assertEquals("hashed-secret", saved.getValue().getPassword());
        assertEquals("ROLE_USER", saved.getValue().getRole());
        assertEquals("Asha", saved.getValue().getName());
    }

    @Test
    void register_emailAlreadyUsed_doesNotSaveAgain() {
        when(userRepository.findByEmail("asha@example.com")).thenReturn(Optional.of(new User()));

        String message = userService.register(newUser());

        assertEquals("Email already registered", message);
        verify(userRepository, never()).save(any());
    }

    private UserDto newUser() {
        UserDto dto = new UserDto();
        dto.setName("Asha");
        dto.setEmail("asha@example.com");
        dto.setPassword("secret123");
        dto.setPhone("9000000000");
        return dto;
    }
}
