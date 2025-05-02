package edu.cit.AssessMate.Config;

import edu.cit.AssessMate.Model.ERole;
import edu.cit.AssessMate.Model.Role;
import edu.cit.AssessMate.Model.User;
import edu.cit.AssessMate.Repository.RoleRepository;
import edu.cit.AssessMate.Repository.UserRepository;
import edu.cit.AssessMate.Service.impl.UserDetailsImpl;
import edu.cit.AssessMate.security.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private static final Logger logger = LoggerFactory.getLogger(OAuth2SuccessHandler.class);
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public OAuth2SuccessHandler(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                       Authentication authentication) throws IOException {
        logger.info("OAuth2 authentication success handler called");
        
        try {
            if (!(authentication instanceof OAuth2AuthenticationToken)) {
                logger.error("Authentication is not an OAuth2AuthenticationToken: {}", 
                          authentication.getClass().getName());
                redirectWithError(response, "Invalid token type");
                return;
            }

            OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
            OAuth2User oauth2User = token.getPrincipal();
            Map<String, Object> attributes = oauth2User.getAttributes();
            
            String email = (String) attributes.get("email");
            String fullName = (String) attributes.get("name");
            
            if (email == null) {
                logger.error("Email is null in OAuth2 user attributes");
                redirectWithError(response, "Email not provided by OAuth provider");
                return;
            }
            
            String username = email.split("@")[0];
            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> createNewUser(username, email, fullName));
            
            UserDetailsImpl userDetails = UserDetailsImpl.build(user);
            Authentication userAuthentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            
            String jwt = jwtUtils.generateJwtToken(userAuthentication);
            boolean needsRoleSelection = user.getRoles().isEmpty();
            
            // Create JSON data with proper escaping
            String userData = String.format("{"
                + "\"id\": %d,"
                + "\"username\": \"%s\","
                + "\"email\": \"%s\","
                + "\"fullName\": \"%s\","
                + "\"token\": \"%s\","
                + "\"needsRoleSelection\": %b"
                + "}", 
                user.getId(),
                escapeJavaScript(user.getUsername()),
                escapeJavaScript(user.getEmail()),
                escapeJavaScript(user.getFullName()),
                escapeJavaScript(jwt),
                needsRoleSelection
            );

            response.setContentType("text/html");
            response.getWriter().write(
                "<html><body><script>" +
                "try {" +
                "  const userData = " + userData + ";" +
                "  if (window.opener) {" +
                "    window.opener.postMessage({" +
                "      type: 'oauth2-success'," +
                "      userData: userData," +
                "      token: userData.token," +
                "      needsRoleSelection: userData.needsRoleSelection" +
                "    }, '*');" +
                "    setTimeout(function() { window.close(); }, 1000);" +
                "  } else {" +
                "    sessionStorage.setItem('user', JSON.stringify(userData));" +
                "    sessionStorage.setItem('token', userData.token);" +
                "    window.location.replace(userData.needsRoleSelection " +
                "      ? 'http://localhost:5173/role-selection'" +
                "      : 'http://localhost:5173/dashboard');" +
                "  }" +
                "} catch (e) {" +
                "  console.error('Error:', e);" +
                "  window.location.replace('http://localhost:5173/login?error=' + encodeURIComponent(e.message));" +
                "}" +
                "</script></body></html>"
            );
            
        } catch (Exception e) {
            logger.error("OAuth2 authentication error: {}", e.getMessage(), e);
            redirectWithError(response, "OAuth2 authentication failed: " + e.getMessage());
        }
    }
    
    private User createNewUser(String username, String email, String fullName) {
        logger.info("Creating new user for OAuth2 login: {}", email);
        
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setFullName(fullName != null ? fullName : username);
        newUser.setPassword(passwordEncoder.encode("oauth2-user-" + System.currentTimeMillis()));
        newUser.setRoles(new HashSet<>()); // No roles assigned initially
        
        // Log the new user creation
        logger.debug("New user created: {}", newUser);

        return userRepository.save(newUser);
    }
    
    private void redirectWithError(HttpServletResponse response, String errorMessage) throws IOException {
        response.setContentType("text/html");
        response.getWriter().write(
            "<html><body><script>" +
            "try {" +
            "  if (window.opener) {" +
            "    window.opener.postMessage({type: 'oauth2-error', error: '" + errorMessage + "'}, 'http://localhost:5173');" +
            "    setTimeout(function() { window.close(); }, 500);" +
            "  } else {" +
            "    window.location.href = 'http://localhost:5173/login?error=" + encodeURIComponent(errorMessage) + "';" +
            "  }" +
            "} catch (e) {" +
            "  console.error('Error in OAuth2 error callback: ' + e.message);" +
            "  window.location.href = 'http://localhost:5173/login?error=' + encodeURIComponent('OAuth2 error callback failed: ' + e.message');" +
            "}" +
            "</script></body></html>"
        );
    }

    private String encodeURIComponent(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String escapeJavaScript(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\r", "\\r")
                   .replace("\n", "\\n")
                   .replace("\t", "\\t");
    }
}