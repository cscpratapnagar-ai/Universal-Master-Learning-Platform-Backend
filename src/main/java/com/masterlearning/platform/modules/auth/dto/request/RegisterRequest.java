package com.masterlearning.platform.modules.auth.dto.request;
import jakarta.validation.constraints.*;
public record RegisterRequest(@NotBlank @Email @Size(max=255) String email,@NotBlank @Size(min=8,max=128) String password,@NotBlank @Size(max=100) String firstName,@Size(max=100) String lastName){}