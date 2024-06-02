package com.app.backend.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Service
public class JwtService {
    @Value("${application.security.jwt.secret_key}")
    private String secretKey;
    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    public String generateToken(Authentication authentication) {
        Date tokenCreateDate = new Date();
        Date tokenExpirationDate = new Date(tokenCreateDate.getTime() + jwtExpiration);

        return Jwts.builder().setSubject(authentication.getName()).setIssuedAt(tokenCreateDate).setExpiration(tokenExpirationDate).signWith(getSignInKey()).compact();
    }

    public String extractEmail(String token) {
        try {
            Claims claims = decodeToken(token);
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isTokenValid(String token, UserDetails userDetails) throws Exception {
        final String username = extractEmail(token);
        return (username != null && username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private Claims decodeToken(String token) throws Exception {
        return Jwts.parser().verifyWith((SecretKey) getSignInKey()).build().parseSignedClaims(token).getPayload();
    }

    private boolean isTokenExpired(String token) throws Exception {
        return decodeToken(token).getExpiration().before(new Date());
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
