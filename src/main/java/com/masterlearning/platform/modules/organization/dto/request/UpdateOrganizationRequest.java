package com.masterlearning.platform.modules.organization.dto.request;
import jakarta.validation.constraints.*;
public record UpdateOrganizationRequest(@NotBlank @Size(max=200) String name,@Size(max=500) String description){}