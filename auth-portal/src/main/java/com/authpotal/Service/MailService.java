package com.authpotal.Service;

import java.util.Optional;

import com.authpotal.Config.Jwt.JwtUtil;
import com.authpotal.Entity.User;
import com.authpotal.Exception.UserNotFoundException;
import com.authpotal.Repository.UserRepository;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;

@ApplicationScoped
public class MailService {

    @Inject
    UserRepository userRepository;

    @Inject
    JwtUtil jwtUtil;

    @Inject
    Mailer mailer;

    public void verifyUser(String email) throws UserNotFoundException {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            throw new UserNotFoundException("User Not Found");
        }
        // token 1 minute expiration
        String token = jwtUtil.getTokenFromEmail(email);
        String link = "http://localhost:8088/api/mail/verify?token=" + token;

        String subject = "Verify your email";
        String body = "Click the link to verify your account (valid for 1 minute):\n\n" + link;

        sendMail(user.getEmail(), subject, body);

        System.out.println(" Verification email sent to: " + user.getEmail());
    }

    public void sendMail(String to, String subject, String body) {
        try {
            mailer.send(
                    Mail.withText(to, subject, body)

            );
            System.out.println(" Email sent to " + to);
        } catch (Exception e) {
            throw new BadRequestException("Email Not send");
        }
    }

    @Transactional
    public void setUserVerified(String email) throws UserNotFoundException {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            throw new UserNotFoundException("User Not Found");
        }
        System.out.println(user.fullName);

        user.setVerified(true);
        userRepository.persist(user);
        System.out.println("User marked as verified.");
    }

    public String verifyUserlogin(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            return "not_found";
        }

        User user = userOptional.get();

        if (!user.isVerified()) {
            return "not_verified";
        }

        return "verified";
    }
}
