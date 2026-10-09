package com.queueless.queueless.user;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "app_users", uniqueConstraints = @UniqueConstraint(name="uk_app_users_username", columnNames="username"))
public class AppUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable=false, unique=true, length=50) private String username;
    @Column(nullable=false) private String passwordHash;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=20) private UserRole role;
    @Column(nullable=false, updatable=false) private Instant createdAt;
    public AppUser() {}
    public AppUser(String username, String passwordHash, UserRole role) { this.username=username; this.passwordHash=passwordHash; this.role=role; }
    @PrePersist void onCreate() { if (createdAt == null) createdAt=Instant.now(); }
    public Long getId(){return id;} public String getUsername(){return username;} public String getPasswordHash(){return passwordHash;} public UserRole getRole(){return role;} public Instant getCreatedAt(){return createdAt;}
    public void setUsername(String v){username=v;} public void setPasswordHash(String v){passwordHash=v;} public void setRole(UserRole v){role=v;}
}
