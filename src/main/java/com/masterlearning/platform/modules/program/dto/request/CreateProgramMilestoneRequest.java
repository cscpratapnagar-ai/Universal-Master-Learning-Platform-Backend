package com.masterlearning.platform.modules.program.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record CreateProgramMilestoneRequest(
    @NotBlank @Size(max=180) String title,
    @Size(max=2500) String description,
    LocalDate dueDate,
    @Min(0) int sortOrder
) {}
