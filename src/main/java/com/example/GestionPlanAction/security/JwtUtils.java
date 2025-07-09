package com.example.GestionPlanAction.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${app.jwtSecret:mySecretKey}")
    private String jwtSecret;

    @Value("${app.jwtExpirationMs:86400000}") // 24 hours
    private int jwtExpirationMs;

    public String generateJwtToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        // Create claims with roles information
        Claims claims = Jwts.claims().setSubject(userPrincipal.getUsername());
        
        // Add roles to the token claims
        claims.put("roles", userPrincipal.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .toList());
        
        // Log for debugging
        logger.debug("Generating token for user {} with roles: {}", 
            userPrincipal.getUsername(), 
            claims.get("roles"));
        
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS512)
                .compact();
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }
    
    // Add method to extract roles from JWT token
    public List<SimpleGrantedAuthority> getRolesFromJwtToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
                
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        
        try {
            // Try to get roles from the claims - handle different possible formats
            Object rolesObj = claims.get("roles");
            
            if (rolesObj instanceof List) {
                // Standard list format
                @SuppressWarnings("unchecked")
                List<Object> rolesList = (List<Object>) rolesObj;
                
                for (Object roleObj : rolesList) {
                    String role = roleObj.toString();
                    // Make sure role has ROLE_ prefix
                    if (!role.startsWith("ROLE_")) {
                        role = "ROLE_" + role;
                    }
                    authorities.add(new SimpleGrantedAuthority(role));
                }
                
                logger.debug("Extracted roles from token as List: {}", rolesList);
            } else if (rolesObj instanceof String) {
                // Single role as string
                String role = (String) rolesObj;
                if (!role.startsWith("ROLE_")) {
                    role = "ROLE_" + role;
                }
                authorities.add(new SimpleGrantedAuthority(role));
                
                logger.debug("Extracted single role from token: {}", role);
            } else if (rolesObj != null) {
                // Some other format, try toString
                String roleStr = rolesObj.toString();
                logger.debug("Extracted unknown format role from token: {}", roleStr);
                if (!roleStr.startsWith("ROLE_")) {
                    roleStr = "ROLE_" + roleStr;
                }
                authorities.add(new SimpleGrantedAuthority(roleStr));
            } else {
                logger.warn("No roles found in token for user: {}", claims.getSubject());
                
                // Try to look for other possible fields
                for (String key : claims.keySet()) {
                    logger.debug("Token claim key: {} with value type: {}", key, 
                        claims.get(key) != null ? claims.get(key).getClass().getName() : "null");
                }
            }
        } catch (Exception e) {
            logger.error("Error extracting roles from token: {}", e.getMessage(), e);
        }
        
        // If we still have no authorities, add a default one
        if (authorities.isEmpty()) {
            logger.warn("No authorities could be extracted from token, adding default ROLE_USER");
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }
        
        return authorities;
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }
    
    public String generateTokenFromUsername(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS512)
                .compact();
    }
    
    // New method to generate token with roles
    public String generateTokenFromUsernameWithRoles(String username, Collection<? extends GrantedAuthority> authorities) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("roles", authorities.stream()
                .map(authority -> authority.getAuthority())
                .toList());
                
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS512)
                .compact();
    }
}