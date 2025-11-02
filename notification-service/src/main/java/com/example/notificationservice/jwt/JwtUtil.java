package com.example.notificationservice.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
public class JwtUtil {
    public static final String SECRET = "56928731907473259834758923975834001978431540789351748901579408315709843175089192839123821057984879453897";

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String ExtractUserId(HttpServletRequest request) {
        String auHeader = request.getHeader("Authorization");

        if (auHeader != null && auHeader.startsWith("Bearer ")) {
            String token = auHeader.substring(7);

            try {
                Claims claims = getClaims(token);
                String userId = claims.get("userId", String.class);
                return userId;
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to extract userId from token: " + e.getMessage(), e);
            }
        } else {
            throw new RuntimeException("Authorization header is missing or invalid");
        }
    }
}

