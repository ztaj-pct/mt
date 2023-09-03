//package com.pct.device.config;
//
//import io.jsonwebtoken.*;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//@Component
//public class HttpRequestInterceptor extends HandlerInterceptorAdapter {
//
//    private static final Logger logger = LoggerFactory.getLogger(HttpRequestInterceptor.class);
//
//    private final String jwtSecret = "JWTSuperSecretKey";
//
//    @Override
//    public boolean preHandle(HttpServletRequest requestServlet, HttpServletResponse responseServlet, Object handler) throws Exception {
//        try {
//           getTokenFromRequest(requestServlet);
//        } catch (Exception e) {
//            responseServlet.getWriter().write(e.getMessage());
//            responseServlet.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//            return false;
//        }
//        return true;
//    }
//
//    public String getTokenFromRequest(HttpServletRequest request) throws Exception {
//        String token = null;
//        String bearerToken = request.getHeader("Authorization");
//        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
//            token = bearerToken.substring(7);
//        }
//        if (validateToken(token)) {
//            return token;
//        } else {
//            throw new Exception("Error validating token");
//        }
//    }
//
//    public boolean validateToken(String authToken) throws Exception {
//        try {
//            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
//            return true;
//        } catch (SignatureException ex) {
//            logger.error("Invalid JWT signature");
//        } catch (MalformedJwtException ex) {
//            logger.error("Invalid JWT token");
//        } catch (ExpiredJwtException ex) {
//            logger.error("Expired JWT token");
//            throw new Exception("Your session has expired. Please login again.");
//
//        } catch (UnsupportedJwtException ex) {
//            logger.error("Unsupported JWT token");
//        } catch (IllegalArgumentException ex) {
//            logger.error("JWT claims string is empty.");
//        }
//        return false;
//    }
//}
