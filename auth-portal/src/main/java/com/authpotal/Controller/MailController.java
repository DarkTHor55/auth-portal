package com.authpotal.Controller;

import java.time.Instant;

import org.eclipse.microprofile.jwt.JsonWebToken;

import com.authpotal.Config.Jwt.JwtUtil;
import com.authpotal.Response.MessageResponse;
import com.authpotal.Service.MailService;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/mail")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MailController {

    @Inject
    MailService mailService;
    @Inject
    JwtUtil jwtUtil;

    @Inject
    io.smallrye.jwt.auth.principal.JWTParser jwtParser;

    // STEP 1: Send verification email
    @GET
    @Path("/send-verification")
    @Produces(MediaType.TEXT_PLAIN)
    public Response sendVerification(@QueryParam("email") String email) {

        try {
            mailService.verifyUser(email);
            return Response.ok("Verification email sent to " + email).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(" Failed to send verification email: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/verify")
    @Produces(MediaType.TEXT_PLAIN)
    public Response verifyEmail(@QueryParam("token") String token) {
        try {
            JsonWebToken jwt = jwtParser.parse(token);
            if (jwt.getExpirationTime() < Instant.now().getEpochSecond()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(" Token expired. Please request a new verification email.")
                        .build();
            }
            String email = jwtUtil.extractEmail(token);
            System.out.println("EMAIL:" + email);
            mailService.setUserVerified(email);
            return Response.ok(" Email verified successfully! You can now log in.").build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(" Verification failed: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/login-verify")
    @Produces(MediaType.APPLICATION_JSON)
    public Response loginVerify(@QueryParam("email") String email) {
        try {
            String status = mailService.verifyUserlogin(email);

            if (status.equals("verified")) {
                return Response.ok(new MessageResponse("Login allowed", "success")).build();
            } else if (status.equals("not_verified")) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(new MessageResponse("Please verify your email", "not_verified"))
                        .build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new MessageResponse("User not found", "not_found"))
                        .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new MessageResponse("Error: " + e.getMessage(), "error"))
                    .build();
        }
    }

}
