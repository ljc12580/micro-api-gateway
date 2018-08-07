package com.glodon.zuul.service;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.security.sasl.AuthenticationException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by liuqc-b on 2018/7/9.
 */

@Service
public class AuthService {

    private static final String CLAIM_KEY_USERNAME = "userName";
    private static final String CLAIM_KEY_CREATED = "created";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.refreshexpire}")
    private Long refreshExpire;


    //获取token
    public Map<String, String> createToken(String username, String password) {
        Claims claims = Jwts.claims().setSubject("glodon");
        claims.put(CLAIM_KEY_USERNAME, username);
        Map<String, String> map = new HashMap<String, String>();
        String accesstoken = generateToken(claims);
        String refreshtoken = generateRefreshToken(claims);
        map.put("accesstoken", accesstoken);
        map.put("refreshtoken", refreshtoken);
        return map;
    }



    /**
     * 根据token获取claims
     *
     * @param token
     * @return
     */
    public Claims getClaimsFromToken(String token) throws Exception {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
            return claims;
        } catch (UnsupportedJwtException | MalformedJwtException | IllegalArgumentException | SignatureException ex) {
            throw new UnsupportedJwtException("Invalid JWT token: ", ex);
        } catch (ExpiredJwtException expiredEx) {
            throw new AuthenticationException("JWT Token expired", expiredEx);
        }
    }

    /**
     * 生成accesstoken
     *
     * @param claims
     * @return
     */
    public String generateToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(generateExpirationDate())
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    /**
     * 生成refreshtoken
     *
     * @param claims
     * @return
     */
    public String generateRefreshToken(Map<String, Object> claims) {
        claims.put(CLAIM_KEY_CREATED, new Date());
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(generateRefreshExpirationDate())
                .setId(UUID.randomUUID().toString())
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    private Date generateExpirationDate() {
        return new Date(System.currentTimeMillis() + expiration * 1000);
    }

    private Date generateRefreshExpirationDate() {
        return new Date(System.currentTimeMillis() + refreshExpire * 1000);
    }

}