package com.ppp.common.security.jwt;

import com.ppp.common.exception.ErrorCode;
import com.ppp.common.exception.TokenException;
import com.ppp.domain.user.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Date;

@Slf4j
@Service
public class JwtTokenProvider {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;
    @Value("${application.security.jwt.refresh-token.secret-key}")
    private String secretRefreshKey;
    @Value("${application.security.jwt.expiration}")
    private long accessExpiration;
    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";

    public String generateAccessToken(User user){
        Date expireDate = createExpireDate(accessExpiration);
        Key key = Keys.hmacShaKeyFor(secretKey.getBytes());
        Claims claim = Jwts.claims().setSubject(user.getEmail());

        return Jwts.builder()
                .setClaims(claim)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(expireDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(User user){
        Date expireDate = createExpireDate(refreshExpiration);
        Key key = Keys.hmacShaKeyFor(secretRefreshKey.getBytes());
        Claims claim = Jwts.claims().setSubject(user.getEmail());

        return Jwts.builder()
                .setClaims(claim)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(expireDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private Date createExpireDate(long expirationInMs){
        long expireTime = new Date().getTime() + expirationInMs;
        return new Date(expireTime);
    }

    public Long getAccessExpiration(String accessToken) {
        // accessToken 남은 유효시간
        Date expiration = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .build()
                .parseClaimsJws(accessToken)
                .getBody()
                .getExpiration();
        // 현재 시간
        Long now = new Date().getTime();
        return (expiration.getTime() - now);
    }

    public Long getRefreshExpiration(String refreshToken) {
        // refreshToken 남은 유효시간
        Date expiration = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secretRefreshKey.getBytes()))
                .build()
                .parseClaimsJws(refreshToken)
                .getBody()
                .getExpiration();
        // 현재 시간
        Long now = new Date().getTime();
        return (expiration.getTime() - now);
    }

    public String getUserEmailFromAccessToken(String token){
        Claims claim = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claim.getSubject();
    }
    public String getUserEmailFromRefreshToken(String token){
        Claims claim = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secretRefreshKey.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claim.getSubject();
    }

    public String getJwtFromRequestHeader(HttpServletRequest request){
        String token = request.getHeader(HEADER_STRING);
        if(StringUtils.hasText(token) && token.startsWith(TOKEN_PREFIX)){
            return token.substring(TOKEN_PREFIX.length());
        }
        else return token;
    }

    public boolean validateAccessToken(String token){
        Key key= Keys.hmacShaKeyFor(secretKey.getBytes());
        return validateToken(token, key);
    }

    public boolean validateRefreshToken(String token){
        Key key = Keys.hmacShaKeyFor(secretRefreshKey.getBytes());
        return validateToken(token, key);
    }

    private boolean validateToken(String token, Key key){
        try{
            Jwts.parserBuilder().setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
            // EntryPoint 로 이동
        }catch(io.jsonwebtoken.security.SecurityException ex) {
            log.error("Invalid JWT signature");
            throw new TokenException(ErrorCode.INVALID_SIGNATURE);
        }catch(MalformedJwtException ex) {
            log.error("Invalid JWT token");
            throw new TokenException(ErrorCode.MALFORMED_TOKEN);
        }catch(ExpiredJwtException ex) {
            throw new TokenException(ErrorCode.EXPIRED_TOKEN);
        }catch(UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
            throw new TokenException(ErrorCode.UNSUPPORTED_TOKEN);
        }catch(IllegalArgumentException ex) {
            log.error("JWT claims string is empty");
            throw new TokenException(ErrorCode.ILLEGALARGUMENT_TOKEN);
        }
    }
}
