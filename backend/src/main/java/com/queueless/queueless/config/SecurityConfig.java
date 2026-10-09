package com.queueless.queueless.config;

import com.queueless.queueless.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean; import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; import org.springframework.security.config.Customizer; import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity; import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.authentication.AuthenticationManager; import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain; import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration; import org.springframework.web.cors.CorsConfigurationSource; import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;
@Configuration @EnableMethodSecurity
public class SecurityConfig {
 @Bean SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
  return http.cors(Customizer.withDefaults()).csrf(csrf->csrf.disable()).formLogin(form->form.disable()).httpBasic(basic->basic.disable())
   .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
   .exceptionHandling(e->e.authenticationEntryPoint((req,res,ex)->res.sendError(401,"Authentication required"))
      .accessDeniedHandler((req,res,ex)->res.sendError(403,"Access denied")))
   .authorizeHttpRequests(a->a.requestMatchers(HttpMethod.OPTIONS,"/**").permitAll()
    .requestMatchers("/api/v1/health","/api/v1/auth/login").permitAll()
    .requestMatchers(HttpMethod.GET,"/api/v1/queues/**").permitAll()
    .requestMatchers(HttpMethod.POST,"/api/v1/queues/*/join").permitAll()
    .requestMatchers(HttpMethod.POST,"/api/v1/queues").hasAnyRole("STAFF","ADMIN")
    .requestMatchers(HttpMethod.POST,"/api/v1/queues/*/call-next").hasAnyRole("STAFF","ADMIN")
    .requestMatchers(HttpMethod.PATCH,"/api/v1/queues/*/entries/*/status").hasAnyRole("STAFF","ADMIN")
    .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
    .anyRequest().authenticated())
   .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class).build();
 }
 @Bean AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception { return config.getAuthenticationManager(); }
 @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
 @Bean CorsConfigurationSource corsConfigurationSource(){ CorsConfiguration c=new CorsConfiguration(); c.setAllowedOrigins(List.of("http://localhost:5173","http://127.0.0.1:5173")); c.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS")); c.setAllowedHeaders(List.of("Authorization","Content-Type")); c.setAllowCredentials(false); UrlBasedCorsConfigurationSource s=new UrlBasedCorsConfigurationSource(); s.registerCorsConfiguration("/api/**",c); return s; }
}
