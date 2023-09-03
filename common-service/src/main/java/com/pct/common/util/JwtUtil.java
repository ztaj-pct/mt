package com.pct.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    private final String jwtSecret = "JWTSuperSecretKey";

    public Long getUserIdFromRequest(HttpServletRequest request) {
        String token = getTokenFromRequest(request);

        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();

        return Long.parseLong(claims.getSubject());
    }

    public List<String> getUserRoleFromRequest(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();

        return (List<String>) claims.get("scopes");
    }

    public Long getUserCompanyIdFromRequest(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();

        return Long.valueOf((Integer) claims.get("companyId"));
    }

    public String getTokenFromRequest(HttpServletRequest request) {
        String token = null;
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            token = bearerToken.substring(7);
        }
        return token;
    }
    public String getUserFromContaxt() {
    UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication()
			.getPrincipal();
	String username = userDetails.getUsername();
	return username;
    }
}
