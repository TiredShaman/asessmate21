package edu.cit.AssessMate.security;

import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import edu.cit.AssessMate.Service.impl.UserDetailsImpl; // Assuming this class exists and implements UserDetails
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException; // Import SignatureException

/**
 * Utility class for generating, validating, and parsing JWT tokens.
 * Includes enhanced logging for debugging validation failures.
 */
@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    // Inject JWT secret and expiration from application properties
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private int jwtExpirationMs;

    /**
     * Generates a JWT token for the given authentication object.
     * Includes username as subject and roles as a claim.
     */
    public String generateJwtToken(Authentication authentication) {
        // Get the principal (UserDetailsImpl) from the authentication object
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        // Collect user authorities (roles) into a comma-separated string
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority) // Get the authority string (e.g., "ROLE_TEACHER")
                .collect(Collectors.joining(",")); // Join them with a comma

        logger.info("JwtUtils - Generating JWT for user: {}, roles: {}", userPrincipal.getUsername(), authorities);

        // Build the JWT token
        return Jwts.builder()
                .setSubject(userPrincipal.getUsername()) // Set the subject (typically username)
                .claim("roles", authorities) // Add roles as a custom claim
                .setIssuedAt(new Date()) // Set the issue date
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs)) // Set the expiration date
                .signWith(key(), SignatureAlgorithm.HS256) // Sign the token with the secret key and algorithm
                .compact(); // Compact the token into a string
    }

    /**
     * Gets the signing key from the base64 encoded secret.
     */
    private Key key() {
        // Decode the base64 secret key
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        logger.debug("JwtUtils - Using JWT secret (decoded byte length): {}", keyBytes.length);
        // Create a signing key
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extracts the username (subject) from a JWT token.
     */
    public String getUserNameFromJwtToken(String token) {
        // Parse claims without validation first to get the subject, validation happens in validateJwtToken
         try {
             // Note: This method is typically called *after* validateJwtToken returns true.
             // If called before, it might throw exceptions for invalid tokens.
             String username = Jwts.parserBuilder().setSigningKey(key()).build()
                     .parseClaimsJws(token).getBody().getSubject(); // Parse, get body, get subject
             logger.debug("JwtUtils - Extracted username: {}", username); // Changed to debug
             return username;
         } catch (Exception e) {
             // Log and return null if username extraction fails (e.g., token is malformed or signature is bad)
             logger.error("JwtUtils - Error extracting username from token: {}", e.getMessage(), e);
             return null;
         }
    }

    /**
     * Validates a JWT token.
     * Checks signature, expiration, and other properties.
     */
    public boolean validateJwtToken(String authToken) {
        logger.debug("JwtUtils - Validating JWT token: {}", authToken);
        try {
            // Parse and validate the token
            Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken);
            logger.debug("JwtUtils - JWT token validated successfully");
            return true; // Token is valid
        } catch (SignatureException e) {
            logger.error("JwtUtils - Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("JwtUtils - Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JwtUtils - JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JwtUtils - JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JwtUtils - JWT claims string is empty: {}", e.getMessage());
        } catch (Exception e) { // Catch any other unexpected exceptions during parsing/validation
             logger.error("JwtUtils - An unexpected error occurred during token validation: {}", e.getMessage(), e);
        }
        // Return false if any exception occurs during validation
        return false;
    }

    // Getter for the secret (potentially for testing or logging, be cautious exposing secrets)
    public String getJwtSecret() {
        return jwtSecret;
    }
}
