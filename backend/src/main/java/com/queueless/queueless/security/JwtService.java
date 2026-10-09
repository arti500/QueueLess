package com.queueless.queueless.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey key;
    private final long ttlSeconds;
    public JwtService(@Value("${app.jwt.secret}") String secret, @Value("${app.jwt.ttl-seconds:3600}") long ttlSeconds) {
        byte[] bytes;
        try { bytes=Decoders.BASE64.decode(secret); } catch (RuntimeException ex) { bytes=secret.getBytes(java.nio.charset.StandardCharsets.UTF_8); }
        if (bytes.length < 32) throw new IllegalArgumentException("JWT secret must be at least 32 bytes (use a strong base64 secret)");
        this.key=Keys.hmacShaKeyFor(bytes); this.ttlSeconds=ttlSeconds;
    }
    public String generateToken(UserDetails user) {
        Instant now=Instant.now();
        return Jwts.builder().subject(user.getUsername()).issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(ttlSeconds))).signWith(key).compact();
    }
    public String extractUsername(String token) { return parse(token).getSubject(); }
    public boolean isTokenValid(String token, UserDetails user) { try { Claims c=parse(token); return c.getSubject().equals(user.getUsername()) && c.getExpiration().after(new Date()); } catch (RuntimeException e) { return false; } }
    public long getTtlSeconds(){return ttlSeconds;}
    private Claims parse(String token) { return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload(); }
}
