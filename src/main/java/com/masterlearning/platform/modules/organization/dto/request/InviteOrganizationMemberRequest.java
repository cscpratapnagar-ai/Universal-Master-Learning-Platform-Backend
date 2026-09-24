package com.masterlearning.platform.modules.organization.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record InviteOrganizationMemberRequest(
        @NotBlank @Email String email
) {}
