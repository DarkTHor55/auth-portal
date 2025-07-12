package com.authpotal.Service;

import java.time.LocalDateTime;
import java.util.Map;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.authpotal.Config.Jwt.JwtUtil;
import com.authpotal.DTO.UserLoginDTO;
import com.authpotal.DTO.UserRegisterDTO;
import com.authpotal.Entity.User;
import com.authpotal.Enum.Role;
import com.authpotal.Exception.PasswordNotHashed;
import com.authpotal.Exception.RoleNotFoundException;
import com.authpotal.Exception.UserAlreadyExistsException;
import com.authpotal.Repository.UserRepository;
import com.authpotal.Response.UserSignupResponse;

import at.favre.lib.crypto.bcrypt.BCrypt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
@Transactional
public class UserService {
    @Inject
    private UserRepository userRepository;

    @ConfigProperty(name = "security.password.salt")
    String salt;
    @Inject
    JwtUtil jwtUtil;

    @Transactional
    public UserSignupResponse register(UserRegisterDTO request)
            throws UserAlreadyExistsException, PasswordNotHashed, RoleNotFoundException {
        User alreadyUser = userRepository.findByEmail(request.email)
                .orElse(null);

        if (alreadyUser != null) {
            throw new UserAlreadyExistsException("User Already Exist with email: " + request.email);
        }

        String hashedPassword;
        try {
            hashedPassword = BCrypt
                    .withDefaults()
                    .hashToString(12, (request.password + salt).toCharArray());
        } catch (Exception e) {
            throw new PasswordNotHashed("Password not hashed");
        }

        if (request.role != Role.ADMIN && request.role != Role.USER) {
            throw new RoleNotFoundException("Role Not Found");
        }

        try {
            User user = User.builder()
                    .email(request.email)
                    .createdAt(LocalDateTime.now())
                    .fullName(request.fullName)
                    .isVerified(false)
                    .password(hashedPassword)
                    .role(request.role)
                    .build();

            userRepository.persist(user);

            // String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
            return new UserSignupResponse(user.email, "Email need to need to verify");
        } catch (Exception e) {
            throw new BadRequestException("Something went wrong while creating user");
        }
    }

    public Map<String, Object> loginUser(UserLoginDTO request) {
        User user = getUserByEmail(request.email);

        // 1. Email verification
        if (!user.isVerified()) {
            throw new RuntimeException("You need to validate your email to access the portal");
        }


        // 2. Password match
        if (!checkPassword(request.password, user.getPassword())) {
            throw new RuntimeException("Invalid email or password.");
        }

        // 3. Token generate
        String token;
        try {
            token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        } catch (Exception e) {
            throw new RuntimeException("Error generating token");
        }

        System.out.println("1111111111111");
        return Map.of(
                "token", token,
                "email", user.getEmail(),
                "role", user.getRole().name());
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public boolean checkPassword(String rawPassword, String hashedPassword) {
        BCrypt.Result result = BCrypt.verifyer()
                .verify((rawPassword + salt).toCharArray(), hashedPassword);

        return result.verified;
    }

	public User getUser(String email) {
		return userRepository.findByEmail(email).orElse(null);
	}

	public void logout(String token) {
		jwtUtil.invalidateToken(token);
	}

    
}