package com.masterlearning.platform.modules.program.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProgramInterventionActionRequest(
    @NotBlank @Size(max=60) String actionType,
    @Size(max=3000) String note
) {}