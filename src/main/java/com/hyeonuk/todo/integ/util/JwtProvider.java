package com.hyeonuk.todo.integ.util;

import com.hyeonuk.todo.member.entity.Authority;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtProvider {
    @Value("${jwt.secret_key}")
    private String SECRET_KEY;

    @Value("${jwt.exp}")
    private long exp;

    private final UserDetailsService userDetailsService;

    public String createToken(String id, List<Authority> roles){
        return Jwts.builder()
                .setHeaderParam("typ","JWT")
                .setSubject(id)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()+exp))
                .claim("roles",roles)
                .signWith(SignatureAlgorithm.HS256,SECRET_KEY.getBytes(StandardCharsets.UTF_8))
                .compact();
    }

    public Authentication getAuthentication(String token){
        UserDetails userDetails = userDetailsService.loadUserByUsername(this.getId(token));
        return new UsernamePasswordAuthenticationToken(userDetails,"",userDetails.getAuthorities());
    }

    public String getId(String token){
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY.getBytes(StandardCharsets.UTF_8))
                .build()
                .parseClaimsJws(token)
                .getBody().getSubject();
    }

    public boolean isValidToken(String accessToken) {
        try{
            return !Jwts.parserBuilder().setSigningKey(SECRET_KEY.getBytes()).build().parseClaimsJws(accessToken)
                    .getBody().getExpiration().before(new Date());
        }catch(SignatureException e){
            log.error("Invalid JWT Signature",e);
        }catch(MalformedJwtException e){
            log.error("Invalid JWT Token",e);
        }catch(ExpiredJwtException e){
            log.error("Expired JWT token",e);
        }catch(UnsupportedJwtException e){
            log.error("Unsupported JWT token",e);
        }catch(IllegalArgumentException e){
            log.error("JWT claims string is empty",e);
        } catch (Exception e) {
            log.error("Something Exception");
        }
        return false;
    }
}
