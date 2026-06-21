package com.blank.app.security;

import com.blank.app.util.RedisKeyBuilder;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:604800000}")
    private long expiration;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedisKeyBuilder redisKeyBuilder;

    public String generateToken(JwtUserDetails userDetails) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setSubject(String.valueOf(userDetails.getId()))
                .claim("id", userDetails.getId())
                .claim("uid", userDetails.getUid())
                .claim("username", userDetails.getUsername())
                .claim("email", userDetails.getEmail())
                .claim("identity", userDetails.getIdentity())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    public boolean validateToken(String token) {
        if (isBlacklisted(token)) {
            return false;
        }
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public JwtUserDetails getUserDetails(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();

        return new JwtUserDetails(
                claims.get("id", Integer.class),
                claims.get("uid", String.class),
                claims.get("username", String.class),
                claims.get("email", String.class),
                claims.get("identity", String.class)
        );
    }

    public void blacklistToken(String token) {
        String key = redisKeyBuilder.tokenBlacklist(token);
        redisTemplate.opsForValue().set(key, "1", expiration, TimeUnit.MILLISECONDS);
    }

    private boolean isBlacklisted(String token) {
        String key = redisKeyBuilder.tokenBlacklist(token);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
