package com.masterlearning.platform.core.auth;
import com.masterlearning.platform.core.api.ApiResponse; import jakarta.servlet.http.HttpServletRequest; import jakarta.validation.Valid; import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import static com.masterlearning.platform.core.auth.AuthDtos.*;
@RestController @RequestMapping("/api/v1/auth")
public class AuthController {
 private final AuthService service; public AuthController(AuthService s){service=s;}
 @PostMapping("/register") public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest q){service.register(q);return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Registration created. Email verification is required before login.",null));}
 @PostMapping("/login") public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest q,HttpServletRequest r){return ApiResponse.success("Authenticated",service.login(q,r.getHeader("User-Agent"),r.getRemoteAddr()));}
 @PostMapping("/refresh") public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshRequest q,HttpServletRequest r){return ApiResponse.success("Token refreshed",service.rotate(q.refreshToken(),r.getHeader("User-Agent"),r.getRemoteAddr()));}
 @PostMapping("/logout") public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest q){service.logout(q.refreshToken());return ResponseEntity.noContent().build();}
}