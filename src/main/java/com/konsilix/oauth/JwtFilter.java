package com.konsilix.oauth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.Collections;

@Component
public class JwtFilter extends OncePerRequestFilter {

    // TODO - move secret to application.yml
    private final String jwtSecretString = "bXlfc2VjcmV0X2tleQ==bXlfc2VjcmV0X2tleQ==bXlfc2VjcmV0X2tleQ==";
    private final SecretKey jwtSecret = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecretString));


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = parseToken(request);

        if (token != null) {
            try {
                Jws<Claims> claimsJws = Jwts.parser()
                        .verifyWith(jwtSecret)
                        .build()
                        .parseSignedClaims(token);

                String username = claimsJws.getPayload().getSubject();
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        username, null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authentication);
                System.out.println("Filter: Token validated successfully for user: " + username);
            } catch (JwtException e) {
                System.out.println("Filter: Invalid JWT token: " + e.getMessage());
                // Token is invalid, clear context and proceed
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

    private String parseToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    System.out.println("filter: Parsed token from HttpOnly cookie");
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
