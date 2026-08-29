package com.masterlearning.platform.modules.auth.dto.request;
import jakarta.validation.constraints.*;
public record LoginRequest(@NotBlank @Email String email,@NotBlank String password){}