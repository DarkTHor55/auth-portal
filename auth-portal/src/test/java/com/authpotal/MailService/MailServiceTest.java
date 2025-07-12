package com.authpotal.MailService;

import com.authpotal.Config.Jwt.JwtUtil;
import com.authpotal.Entity.User;
import com.authpotal.Exception.UserNotFoundException;
import com.authpotal.Repository.UserRepository;
import com.authpotal.Service.MailService;
import io.quarkus.mailer.Mailer;
import io.quarkus.mailer.Mail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MailServiceTest {

    @Mock
    Mailer mailer;

    @Mock
    UserRepository userRepository;

    @Mock
    JwtUtil jwtUtil; 

    @InjectMocks
    MailService mailService;

    @Test
    void testVerifyUser_shouldSendEmail() {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        user.setVerified(false);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtUtil.getTokenFromEmail(email)).thenReturn("dummy-token");
        doNothing().when(mailer).send(any(Mail.class));

        assertDoesNotThrow(() -> {
            mailService.verifyUser(email);
        });

        verify(mailer, times(1)).send(any(Mail.class));
    }

    @Test
    void testVerifyUser_userNotFound_shouldThrowException() {
        String email = "notfound@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            mailService.verifyUser(email);
        });
    }

    @Test
    void testSetUserVerified_shouldUpdateUser() throws UserNotFoundException {
        String email = "verified@example.com";
        User user = new User();
        user.setEmail(email);
        user.setVerified(false);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).persist(user);

        mailService.setUserVerified(email);

        assertTrue(user.isVerified());
        verify(userRepository, times(1)).persist(user);
    }

    @Test
    void testSetUserVerified_userNotFound_shouldThrow() {
        String email = "unknown@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            mailService.setUserVerified(email);
        });
    }
}
