package org.iam.util;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.iam.pojo.domain.User;
import org.iam.properties.JwtProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {
    @Autowired
    private JwtProperties jwtProperties;


    public String generateToken(User user) {
        long now = System.currentTimeMillis();
        Date expiryDate = new Date(now + jwtProperties.getTtl());
        JwtBuilder builder = Jwts.builder()
                .setSubject(user.getAccount())
                .setId(String.valueOf(user.getId()))
                .claim("uuid",user.getUserUuid())
                .setIssuedAt(new Date(now))
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtProperties.getSecretKey());

        return builder.compact();
    }

    public Claims validateToken(String token) {
        return Jwts.parser()
                .setSigningKey(jwtProperties.getSecretKey())
                .parseClaimsJws(token)
                .getBody();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.getSubject();
    }
}
