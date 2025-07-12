package com.authpotal.DTO;


import com.authpotal.Enum.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
@Builder
public class UserRegisterDTO {
    @Email
    @NotBlank
    public String email;

    @NotBlank
    public String password;

    @NotBlank
    public String fullName;

    @NotBlank
    public Role role;
}
