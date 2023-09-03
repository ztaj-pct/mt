package com.pct.device.jwt.config;

import static com.pct.device.jwt.config.JwtConstants.JWT_USER_KEY;
import static com.pct.device.jwt.config.JwtConstants.ACCESS_TOKEN_VALIDITY_SECONDS;
import static com.pct.device.jwt.config.JwtConstants.ROLE_ID;
import static com.pct.device.jwt.config.JwtConstants.ROLE_NAME;
import static com.pct.device.jwt.config.JwtConstants.SIGNING_KEY;

import java.io.Serializable;
import java.util.Date;
import java.util.function.Function;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class TokenProvider implements Serializable {

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(SIGNING_KEY)
                .parseClaimsJws(token)
                .getBody();
    }

    public Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public String generateToken(JwtUser jwtUser) {
        
        return Jwts.builder()
                .setSubject(jwtUser.getUsername())
                .claim(ROLE_ID, jwtUser.getRoleId().toString())
                .claim(JWT_USER_KEY, jwtUser)
                .claim(ROLE_NAME, jwtUser.getRoleName())
                .signWith(SignatureAlgorithm.HS256, SIGNING_KEY)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY_SECONDS*1000))
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (
              username.equals(userDetails.getUsername())
                    && !isTokenExpired(token));
    }

    public JwtUser getJwtUser(final String token) {
        final JwtParser jwtParser = Jwts.parser().setSigningKey(SIGNING_KEY);
        final Jws<Claims> claimsJws = jwtParser.parseClaimsJws(token);
        final Claims claims = claimsJws.getBody();
        
        System.out.println(claims.getSubject());
        System.out.println(claims.get(JWT_USER_KEY));
        System.out.println(claims.get(ROLE_ID));
        System.out.println(claims.get(ROLE_NAME));
        
        JwtUser jwtUser = new JwtUser();
        jwtUser.setUsername(claims.getSubject());
        jwtUser.setRoleId((String)claims.get(ROLE_ID));
        jwtUser.setRoleName((String)claims.get(ROLE_NAME));
        return jwtUser;
    }

}
