package com.konsilix.oauth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;

import javax.crypto.SecretKey;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final SecretKey jwtSecret;
    private final long jwtExpirationMs = 3600000; // 1 hour

    // Use constructor injection to receive the property at creation time.
    public AuthController(@Value("${app.jwt.secret:x1yz1234567890}") String jwtSecretString) {
        // This allows you to initialize the final 'jwtSecret' field correctly.
        this.jwtSecret = Keys.hmacShaKeyFor(jwtSecretString.getBytes());
    }
    @GetMapping("/success")
    public ResponseEntity<Map<String, String>> success(Authentication authentication) {
        String token = generateToken(authentication.getName());

        ResponseCookie cookie = ResponseCookie.from("jwt", token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(3600)
                .build();

        return ResponseEntity.status(302)
                .header("Set-Cookie", cookie.toString())
//                .header("Location", "http://localhost:4200") // Redirect to Angular frontend
                .header("Location", "http://localhost:3000") // Redirect to Angular frontend
                .build();
    }

    private String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(jwtSecret)
                .compact();
    }
}
