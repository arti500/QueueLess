package com.queueless.queueless.security;
import com.queueless.queueless.user.AppUserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class DatabaseUserDetailsService implements UserDetailsService {
 private final AppUserRepository users;
 public DatabaseUserDetailsService(AppUserRepository users){this.users=users;}
 @Override @Transactional(readOnly=true) public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
   var u=users.findByUsername(username).orElseThrow(()->new UsernameNotFoundException("Invalid username or password"));
   return User.withUsername(u.getUsername()).password(u.getPasswordHash()).roles(u.getRole().name()).build();
 }
}
