package com.authpotal.Controller;

import java.util.Map;

import com.authpotal.DTO.UserLoginDTO;
import com.authpotal.DTO.UserRegisterDTO;
import com.authpotal.Entity.User;
import com.authpotal.Exception.PasswordNotHashed;
import com.authpotal.Exception.RoleNotFoundException;
import com.authpotal.Exception.UserAlreadyExistsException;
import com.authpotal.Service.UserService;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/user")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserController {

    @Inject
    UserService userService;

    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(UserRegisterDTO request)
            throws UserAlreadyExistsException, PasswordNotHashed, RoleNotFoundException {
        return Response.ok(userService.register(request)).build();
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(UserLoginDTO request) {

        try {

            Map<String, Object> response = userService.loginUser(request);
            return Response.ok(response).build();
        } catch (Exception e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Login failed: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public User getUser(@QueryParam("email") String email) {
        return userService.getUser(email);

    }

    @POST
    @Path("/logout")
    @Produces(MediaType.TEXT_PLAIN)
    public Response logout(@HeaderParam("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing Authorization header").build();
        }

        String token = authHeader.substring("Bearer ".length());

        userService.logout(token);
        return Response.ok("Logged out successfully.").build();
    }

}
