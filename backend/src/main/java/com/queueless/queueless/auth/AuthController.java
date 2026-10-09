package com.queueless.queueless.auth;
import com.queueless.queueless.auth.dto.*; import com.queueless.queueless.security.JwtService; import jakarta.validation.Valid;
import org.springframework.http.HttpStatus; import org.springframework.security.authentication.AuthenticationManager; import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; import org.springframework.security.core.Authentication; import org.springframework.security.core.GrantedAuthority; import org.springframework.web.bind.annotation.*; import org.springframework.web.server.ResponseStatusException;
@RestController @RequestMapping("/api/v1/auth")
public class AuthController {
 private final AuthenticationManager authenticationManager; private final JwtService jwt;
 public AuthController(AuthenticationManager authenticationManager,JwtService jwt){this.authenticationManager=authenticationManager;this.jwt=jwt;}
 @PostMapping("/login") public LoginResponse login(@Valid @RequestBody LoginRequest request){
  try { Authentication a=authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.username(),request.password()));
   String role=a.getAuthorities().stream().map(GrantedAuthority::getAuthority).filter(x->x.startsWith("ROLE_")).map(x->x.substring(5)).findFirst().orElse("STAFF");
   return new LoginResponse(jwt.generateToken((org.springframework.security.core.userdetails.UserDetails)a.getPrincipal()),"Bearer",jwt.getTtlSeconds(),a.getName(),role);
  } catch(org.springframework.security.core.AuthenticationException ex){throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid username or password");}
 }
 @GetMapping("/me") public java.util.Map<String,Object> me(org.springframework.security.core.Authentication auth){return java.util.Map.of("username",auth.getName(),"roles",auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());}
}
