package com.masterlearning.platform.modules.privateTeacher;
import java.math.BigDecimal; import java.util.UUID;
public record PrivateTeacherMatchResponse(UUID teacherId,String teacherName,String headline,String subjects,String languages,String teachingModes,BigDecimal hourlyRate,String currency,int score){}