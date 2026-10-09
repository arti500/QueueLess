package com.queueless.queueless.config;
import com.queueless.queueless.user.*; import org.springframework.beans.factory.annotation.Value; import org.springframework.boot.CommandLineRunner; import org.springframework.context.annotation.Bean; import org.springframework.context.annotation.Configuration; import org.springframework.security.crypto.password.PasswordEncoder;
@Configuration public class BootstrapAdmin {
 @Bean CommandLineRunner createBootstrapAdmin(AppUserRepository users, PasswordEncoder encoder,
  @Value("${APP_ADMIN_USERNAME:}") String username, @Value("${APP_ADMIN_PASSWORD:}") String password) {
  return args -> { if(username.isBlank() && password.isBlank()) return; if(username.isBlank() || password.length()<12) throw new IllegalStateException("Set both APP_ADMIN_USERNAME and APP_ADMIN_PASSWORD (at least 12 characters)");
   if(!users.existsByUsername(username)) users.save(new AppUser(username,encoder.encode(password),UserRole.ADMIN));
  };
 }
}
