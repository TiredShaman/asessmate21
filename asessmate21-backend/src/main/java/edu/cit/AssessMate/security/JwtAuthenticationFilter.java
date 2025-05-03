package edu.cit.AssessMate.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException; // Import UsernameNotFoundException
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Custom filter to process JWT tokens for authentication.
 * Extends OncePerRequestFilter to ensure it's executed only once per request.
 * Includes enhanced logging for debugging authentication issues.
 */
@Component
@RequiredArgsConstructor // Lombok annotation for constructor injection
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    // Dependencies injected by Lombok
    private final JwtUtils jwtUtils; // Utility for JWT operations
    private final UserDetailsService userDetailsService; // Service to load user details

    /**
     * Performs the JWT authentication logic for each request.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Skip JWT authentication for OPTIONS requests (CORS preflight)
        if (request.getMethod().equals("OPTIONS")) {
            logger.debug("Skipping JWT authentication for OPTIONS request");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 1. Parse the JWT token from the request header
            String jwt = parseJwt(request);
            logger.debug("Attempting to parse JWT from request for path: {}", request.getRequestURI()); // Log the path
            logger.debug("Token found: {}", jwt != null);

            // 2. Validate the token and extract username if valid
            if (jwt != null) {
                logger.debug("JWT token found: {}", jwt);
                boolean isTokenValid = jwtUtils.validateJwtToken(jwt);
                logger.debug("JWT token validation result: {}", isTokenValid);

                if (isTokenValid) {
                    String username = jwtUtils.getUserNameFromJwtToken(jwt);
                     if (username == null) {
                          logger.warn("Username could not be extracted from valid JWT token.");
                          // Do not proceed if username is null
                          filterChain.doFilter(request, response);
                          return;
                     }
                    logger.info("JWT is valid for user: {}", username);

                    // 3. Load UserDetails using the extracted username
                    UserDetails userDetails = null;
                    try {
                         userDetails = userDetailsService.loadUserByUsername(username);
                         logger.debug("User details loaded for user: {}", username);
                         if (userDetails != null) {
                             logger.debug("User authorities from UserDetails: {}", userDetails.getAuthorities()); // Log the authorities loaded
                         } else {
                             // This case should ideally be handled by UsernameNotFoundException, but adding a check
                             logger.warn("UserDetailsService returned null for user: {}", username);
                         }
                    } catch (UsernameNotFoundException e) {
                         logger.warn("User not found in database for username from JWT: {}", username);
                         // Do not authenticate if user not found
                         userDetails = null; // Ensure userDetails is null
                    } catch (Exception e) {
                         logger.error("Error loading user details for username {}: {}", username, e.getMessage(), e);
                         // Do not authenticate if there's an error loading user
                         userDetails = null; // Ensure userDetails is null
                    }


                    // 4. Create and set Authentication object ONLY if userDetails were successfully loaded
                    if (userDetails != null) {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities());

                        // Set additional details about the authentication request (e.g., IP address)
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        // 5. Set the Authentication object in the SecurityContextHolder
                        // This makes the user authenticated for the current request
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        logger.info("Authentication set in SecurityContextHolder for user: {}", username);
                    } else {
                         logger.warn("UserDetails could not be loaded for user {}, authentication skipped.", username);
                    }

                } else {
                    logger.warn("JWT is invalid."); // Specific validation error logged in JwtUtils
                }
            } else {
                 logger.debug("No JWT token found in request headers for path: {}", request.getRequestURI());
            }
        } catch (Exception e) {
            // Log any exception during the authentication process (excluding expected JWT validation errors handled in JwtUtils)
            logger.error("An unexpected error occurred during JWT authentication filter process for path {}: {}", request.getRequestURI(), e.getMessage(), e); // Log exception details
        }

        // Continue the filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the JWT token from the Authorization header.
     * Expects the format "Bearer <token>".
     */
    private String parseJwt(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        // Check if the header exists and starts with "Bearer "
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            // Extract the token string after "Bearer "
            return authHeader.substring(7);
        }

        // Return null if no valid token is found
        return null;
    }
}
