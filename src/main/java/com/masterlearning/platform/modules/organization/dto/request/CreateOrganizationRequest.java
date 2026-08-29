package com.masterlearning.platform.modules.organization.dto.request;
import jakarta.validation.constraints.*;
public record CreateOrganizationRequest(@NotBlank @Pattern(regexp="^[A-Z0-9_-]{3,100}$") String code,@NotBlank @Size(max=200) String name,@Size(max=500) String description){}