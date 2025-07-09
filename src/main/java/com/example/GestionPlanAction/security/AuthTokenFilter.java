package com.example.GestionPlanAction.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class AuthTokenFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                String username = jwtUtils.getUserNameFromJwtToken(jwt);
                
                // Get roles from token
                List<SimpleGrantedAuthority> authorities = jwtUtils.getRolesFromJwtToken(jwt);
                
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                // Log authorities comparison for debugging
                logger.debug("Token authorities: {}", authorities.stream()
                    .map(SimpleGrantedAuthority::getAuthority)
                    .collect(Collectors.joining(", ")));
                
                logger.debug("UserDetails authorities: {}", userDetails.getAuthorities().stream()
                    .map(auth -> auth.getAuthority())
                    .collect(Collectors.joining(", ")));
                
                // If token has no authorities but userDetails does, use those instead
                if (authorities.isEmpty() && userDetails.getAuthorities() != null && !userDetails.getAuthorities().isEmpty()) {
                    logger.warn("Token has no authorities for user {}, using authorities from UserDetails", username);
                    authorities = userDetails.getAuthorities().stream()
                        .map(auth -> new SimpleGrantedAuthority(auth.getAuthority()))
                        .collect(Collectors.toList());
                }
                
                // Create authentication with authorities (either from token or userDetails)
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // Log request info for debugging
                logger.debug("Request path: {}, Method: {}", request.getRequestURI(), request.getMethod());
                logger.debug("Setting authentication for user: {} with authorities: {}", username, 
                    authorities.stream()
                        .map(auth -> auth.getAuthority())
                        .collect(Collectors.joining(", ")));
                logger.info("User {} authenticated successfully", username);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                logger.debug("No valid JWT token found");
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e.getMessage());
            e.printStackTrace();
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            String token = headerAuth.substring(7);
            logger.debug("JWT Token found: {}", token.substring(0, Math.min(10, token.length())) + "...");
            return token;
        }

        return null;
    }
}