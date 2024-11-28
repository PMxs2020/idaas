package org.iam.util;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.iam.exception.BaseException;
import org.iam.exception.TokenInvalidException;
import org.iam.pojo.domain.User;
import org.iam.properties.JwtProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;

@Component
@Slf4j
public class JwtUtil {
    @Autowired
    private JwtProperties jwtProperties;


    public String generateIamToken(User user) {
        long now = System.currentTimeMillis();
        Date expiryDate = new Date(now + jwtProperties.getTtl()*1000);
        JwtBuilder builder = Jwts.builder()
                .setSubject(user.getAccount())
                .setId(String.valueOf(user.getId()))
                .claim("uuid",user.getUserUuid())
                .setIssuedAt(new Date(now))
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtProperties.getSecretKey());
        return builder.compact();
    }
    public String generateToken(User user, PrivateKey publicKey) {
        long now = System.currentTimeMillis();
        Date expiryDate = new Date(now + jwtProperties.getTtl()*1000);
        JwtBuilder builder = Jwts.builder()
                .setSubject(user.getAccount())
                .setId(String.valueOf(user.getId()))
                .claim("uuid",user.getUserUuid())
                .setIssuedAt(new Date(now))
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.RS256, publicKey);
        return builder.compact();
    }
    public Claims validateIamToken(String token) {
        Claims claims = null;
        try{
            //验证iamtoken不是验证第三方token
            claims =Jwts.parser()
                    .setSigningKey(jwtProperties.getSecretKey())
                    .parseClaimsJws(token)
                    .getBody();
        }catch (Exception e){
            log.error("失败: {}", e.getMessage(), e);
            throw new TokenInvalidException("无效token");
        }
        return claims;
    }

    public Claims validateToken(String token,String publicKey) {
        return Jwts.parser()
                .setSigningKey(publicKey)
                .parseClaimsJws(token)
                .getBody();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = validateIamToken(token);
        return claims.getSubject();
    }
}
