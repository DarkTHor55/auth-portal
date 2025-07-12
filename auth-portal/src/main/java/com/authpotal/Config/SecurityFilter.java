package com.authpotal.Config;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.authpotal.Config.Jwt.JwtUtil;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class SecurityFilter implements ContainerRequestFilter {

    @Inject
    JwtUtil jwtUtil;

    // Publicly accessible endpoints
    private static final List<String> openEndpoints = Arrays.asList(
            "api/user/login",
            "api/user/register",
            "api/mail/verify",
            "api/mail/login-verify",
            "api/mail/send-verification");

    // Allowed API prefixes
    private static final List<String> allowedApiPrefixes = Arrays.asList(
            "api/user",
            "api/auth",
            "api/mail");

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();
        System.out.println("Requested Path: " + path);

        
        path = path.startsWith("/") ? path.substring(1) : path;
        if ("OPTIONS".equalsIgnoreCase(requestContext.getMethod())) {
            return; 
        }

        // boolean isAllowed = allowedApiPrefixes.stream().anyMatch(path::startsWith);
        boolean isPublic = openEndpoints.stream().anyMatch(path::startsWith);
        if (isPublic) {
            return;
        }

        // Allow open endpoints without JWT
        if (openEndpoints.contains(path)) {
            return;
        }

        // JWT Auth Check
        String authHeader = requestContext.getHeaderString("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Missing or invalid Authorization header").build());
            return;
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token) || jwtUtil.isTokenInvalidated(token)) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Invalid or blacklisted JWT Token").build());
            return;
        }
    }
}
