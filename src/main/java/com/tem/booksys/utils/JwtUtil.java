package com.tem.booksys.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.tem.booksys.config.AppConfigProperties;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    private final String key;
    private final long expirationHours;

    public JwtUtil(AppConfigProperties config) {
        this.key = config.getJwt().getSecret();
        this.expirationHours = config.getJwt().getExpirationHours();
    }

    public String genToken(Map<String, Object> claims) {
        return JWT.create()
                .withClaim("claims", claims)
                .withExpiresAt(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * expirationHours))
                .sign(Algorithm.HMAC256(key));
    }

    public Map<String, Object> parseToken(String token) {
        return JWT.require(Algorithm.HMAC256(key))
                .build()
                .verify(token)
                .getClaim("claims")
                .asMap();
    }
}
