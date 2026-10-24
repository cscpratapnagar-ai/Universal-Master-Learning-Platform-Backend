package com.masterlearning.platform.modules.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CreateUserRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 12, max = 128) String temporaryPassword,
        @NotBlank @Size(max = 100) String firstName,
        @Size(max = 100) String lastName,
        @NotNull @NotEmpty Set<@NotBlank String> roles
) {}
