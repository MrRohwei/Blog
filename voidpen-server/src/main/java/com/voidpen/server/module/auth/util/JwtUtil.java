package com.voidpen.server.module.auth.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    @Value("${voidpen.jwt.secret}")
    private String secret;

    @Value("${voidpen.jwt.expire}")
    private long expireSeconds;

    public String generateToken(Long userId, String username, String role) {
        return Jwts.builder()
            .subject(String.valueOf(userId))
            .claim("username", username)
            .claim("role", role)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expireSeconds * 1000))
            .signWith(getKey())
            .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
            .verifyWith(getKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public long getRemainingSeconds(String token) {
        Date expiration = parseToken(token).getExpiration();
        long remainingMillis = expiration.getTime() - System.currentTimeMillis();
        return Math.max(remainingMillis / 1000, 0);
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
