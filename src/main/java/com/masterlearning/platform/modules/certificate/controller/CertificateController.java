package com.masterlearning.platform.modules.certificate.controller;
import com.masterlearning.platform.common.api.ApiResponse; import com.masterlearning.platform.modules.certificate.entity.Certificate; import com.masterlearning.platform.modules.certificate.repository.CertificateRepository; import com.masterlearning.platform.modules.course.repository.CourseRepository; import com.masterlearning.platform.modules.course.repository.EnrollmentRepository; import com.masterlearning.platform.modules.user.repository.UserRepository; import jakarta.persistence.EntityNotFoundException; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/api/v1/certificates") public class CertificateController {
 private final CertificateRepository certificates; private final CourseRepository courses; private final UserRepository users; private final EnrollmentRepository enrollments;
 public CertificateController(CertificateRepository c,CourseRepository co,UserRepository u,EnrollmentRepository e){certificates=c;courses=co;users=u;enrollments=e;}
 @PostMapping("/courses/{courseId}/users/{userId}/issue")
 public ApiResponse<Map<String,Object>> issue(@PathVariable UUID courseId,@PathVariable UUID userId){
   if(certificates.existsByCourseIdAndUserId(courseId,userId)) throw new IllegalArgumentException("Certificate already issued");
   var course=courses.findById(courseId).orElseThrow(()->new EntityNotFoundException("Course not found")); var user=users.findById(userId).orElseThrow(()->new EntityNotFoundException("User not found"));
   var enrollment=enrollments.findByCourseIdAndUserId(courseId,userId).orElseThrow(()->new EntityNotFoundException("Enrollment not found"));
   if(enrollment.getProgressPercent()<100) throw new IllegalArgumentException("Course must be completed before certificate issuance");
   String number="UMLP-"+UUID.randomUUID().toString().substring(0,8).toUpperCase(); var certificate=certificates.save(new Certificate(number,course,user));
   return ApiResponse.success("Certificate issued",Map.of("id",certificate.getId(),"certificateNumber",certificate.getCertificateNumber()));
 }
 @GetMapping("/verify/{certificateNumber}") public ApiResponse<Map<String,Object>> verify(@PathVariable String certificateNumber){
   var c=certificates.findByCertificateNumber(certificateNumber).orElseThrow(()->new EntityNotFoundException("Certificate not found"));
   return ApiResponse.success("Certificate is valid",Map.of("certificateNumber",c.getCertificateNumber(),"valid",true));
 }
}