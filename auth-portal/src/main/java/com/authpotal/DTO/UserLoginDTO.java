package com.authpotal.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Builder
public class UserLoginDTO {
    @NotBlank
    @Email
    public String email;
    @NotBlank
    public String password;
}
