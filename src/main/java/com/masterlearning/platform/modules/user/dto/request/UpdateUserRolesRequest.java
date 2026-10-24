package com.masterlearning.platform.modules.user.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public record UpdateUserRolesRequest(@NotNull @NotEmpty Set<@NotBlank String> roles) {}