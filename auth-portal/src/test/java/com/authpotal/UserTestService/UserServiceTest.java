package com.authpotal.UserTestService;

import com.authpotal.Config.Jwt.JwtUtil;
import com.authpotal.DTO.UserLoginDTO;
import com.authpotal.DTO.UserRegisterDTO;
import com.authpotal.Entity.User;
import com.authpotal.Enum.Role;
import com.authpotal.Exception.PasswordNotHashed;
import com.authpotal.Exception.RoleNotFoundException;
import com.authpotal.Exception.UserAlreadyExistsException;
import com.authpotal.Repository.UserRepository;
import com.authpotal.Service.UserService;

import at.favre.lib.crypto.bcrypt.BCrypt;
import jakarta.ws.rs.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    JwtUtil jwtUtil;

    @InjectMocks
    UserService userService;

    String salt = "salt123";

    @BeforeEach
    void setup() {
        // Set private field salt manually since @ConfigProperty won't work here
        userService.getClass().getDeclaredFields();
        try {
            var field = UserService.class.getDeclaredField("salt");
            field.setAccessible(true);
            field.set(userService, salt);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testRegister_whenUserAlreadyExists_thenThrowsException() {
        String email = "test123@gmail.com";

        UserRegisterDTO request = UserRegisterDTO.builder()
                .email(email)
                .fullName("Test User")
                .password("pass@123")
                .role(Role.USER)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(new User()));

        assertThrows(UserAlreadyExistsException.class, () -> {
            userService.register(request);
        });

        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void testRegister_whenRoleInvalid_thenThrowsRoleNotFoundException() {
        UserRegisterDTO request = UserRegisterDTO.builder()
                .email("newuser@gmail.com")
                .fullName("Test User")
                .password("pass@123")
                .role(null)  // Invalid role
                .build();

        when(userRepository.findByEmail(request.email)).thenReturn(Optional.empty());

        assertThrows(RoleNotFoundException.class, () -> {
            userService.register(request);
        });
    }

    @Test
    void testRegister_whenNewUser_thenSuccess() throws Exception {
        UserRegisterDTO request = UserRegisterDTO.builder()
                .email("newuser@gmail.com")
                .fullName("New User")
                .password("pass@123")
                .role(Role.USER)
                .build();

        when(userRepository.findByEmail(request.email)).thenReturn(Optional.empty());

        doNothing().when(userRepository).persist(any(User.class));

        var response = userService.register(request);
        assertEquals("newuser@gmail.com", response.getEmail());
    }


    @Test
void testLogin_whenUserNotVerified_thenThrowsException() {
    String email = "unverified@gmail.com";
    String rawPassword = "test123";

    User user = new User();
    user.setEmail(email);
    user.setPassword(BCrypt.withDefaults().hashToString(12, (rawPassword + salt).toCharArray()));
    user.setVerified(false);

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

    UserLoginDTO login = UserLoginDTO.builder()
            .email(email) 
            .password(rawPassword)
            .build();

    RuntimeException ex = assertThrows(RuntimeException.class, () -> {
        userService.loginUser(login);
    });

    assertTrue(ex.getMessage().toLowerCase().contains("validate your email"));
}

@Test
void testLogin_whenPasswordIncorrect_thenThrowsException() {
    String email = "user@gmail.com";

    User user = new User();
    user.setEmail(email);
    user.setVerified(true);
    user.setPassword(BCrypt.withDefaults().hashToString(12, "differentPassword".toCharArray()));

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

    UserLoginDTO login = UserLoginDTO.builder()
            .email(email) 
            .password("wrongPassword") 
            .build();

    RuntimeException ex = assertThrows(RuntimeException.class, () -> {
        userService.loginUser(login);
    });

    assertTrue(ex.getMessage().toLowerCase().contains("invalid email or password"));
}

@Test
void testLogin_whenUserIsVerifiedAndPasswordCorrect_thenReturnsToken() throws Exception {
    String email = "validuser@gmail.com";
    String rawPassword = "securePass";

    String hashedPassword = BCrypt.withDefaults().hashToString(12, (rawPassword + salt).toCharArray());

    User user = new User();
    user.setEmail(email);
    user.setPassword(hashedPassword);
    user.setVerified(true);
    user.setRole(Role.USER);

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    when(jwtUtil.generateToken(email, Role.USER)).thenReturn("mocked-jwt-token");

    UserLoginDTO login = UserLoginDTO.builder()
            .email(email) 
            .password(rawPassword)
            .build();

    Map<String, Object> response = userService.loginUser(login);

    assertEquals("mocked-jwt-token", response.get("token"));
    assertEquals(email, response.get("email"));
    assertEquals("USER", response.get("role"));
}

}
