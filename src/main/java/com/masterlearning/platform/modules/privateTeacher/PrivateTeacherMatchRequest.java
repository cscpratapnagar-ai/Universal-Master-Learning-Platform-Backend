package com.masterlearning.platform.modules.privateTeacher;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
public record PrivateTeacherMatchRequest(@Size(max=120) String subject,@Size(max=120) String language,@Size(max=120) String teachingMode,BigDecimal maxHourlyRate){}