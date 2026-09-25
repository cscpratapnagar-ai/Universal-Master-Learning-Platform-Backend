package com.masterlearning.platform.modules.program.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateProgramDependencyRequest(@NotNull UUID predecessorId,@NotNull UUID successorId) {}
