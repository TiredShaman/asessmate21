package edu.cit.AssessMate.Controller;

import edu.cit.AssessMate.Model.ERole;
import edu.cit.AssessMate.Model.Role;
import edu.cit.AssessMate.Model.User;
import edu.cit.AssessMate.payload.request.LoginRequest;
import edu.cit.AssessMate.payload.request.SignupRequest;
import edu.cit.AssessMate.payload.response.JwtResponse;
import edu.cit.AssessMate.payload.response.MessageResponse;
import edu.cit.AssessMate.Repository.RoleRepository;
import edu.cit.AssessMate.Repository.UserRepository;
import edu.cit.AssessMate.security.JwtUtils;
import edu.cit.AssessMate.Service.impl.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = {"https://assessmatefinal-6cog.vercel.app", "http://localhost:3000"}, 
    maxAge = 3600, 
    allowCredentials = "true")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;

    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository,
                          RoleRepository roleRepository, PasswordEncoder encoder, JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        logger.info("Login attempt for user: {}", loginRequest.getUsername());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
            logger.info("Authentication result: {}", authentication);

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());

            logger.info("JWT generated for user: {}, roles: {}", userDetails.getUsername(), roles);
            return ResponseEntity.ok(new JwtResponse(jwt,
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getEmail(),
                    userDetails.getFullName(),
                    roles));
        } catch (BadCredentialsException e) {
            logger.error("Invalid credentials for user: {}", loginRequest.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Invalid username or password"));
        } catch (Exception e) {
            logger.error("Authentication error for user: {}: {}", loginRequest.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Authentication error: " + e.getMessage()));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        logger.info("Signup attempt for user: {}", signUpRequest.getUsername());
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            logger.warn("Username already taken: {}", signUpRequest.getUsername());
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            logger.warn("Email already in use: {}", signUpRequest.getEmail());
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()),
                signUpRequest.getFullName());

        Set<String> strRoles = signUpRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        logger.info("Received roles in signup request: {}", strRoles);

        if (strRoles == null || strRoles.isEmpty()) {
            Role studentRole = roleRepository.findByName(ERole.ROLE_STUDENT)
                    .orElseThrow(() -> new RuntimeException("Error: Role STUDENT not found."));
            roles.add(studentRole);
            logger.info("Assigned default role: STUDENT");
        } else {
            for (String roleStr : strRoles) {
                logger.info("Processing role: {}", roleStr);
                try {
                    ERole eRole = ERole.valueOf("ROLE_" + roleStr.toUpperCase());
                    Role role = roleRepository.findByName(eRole)
                            .orElseThrow(() -> new RuntimeException("Error: Role " + eRole + " not found."));
                    roles.add(role);
                    logger.info("Assigned role: {}", eRole);
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid role: {}. Assigning default STUDENT role.", roleStr);
                    Role studentRole = roleRepository.findByName(ERole.ROLE_STUDENT)
                            .orElseThrow(() -> new RuntimeException("Error: Role STUDENT not found."));
                    roles.add(studentRole);
                    logger.info("Assigned default role: STUDENT");
                }
            }
        }

        user.setRoles(roles);
        userRepository.save(user);
        logger.info("User registered: {}, roles: {}", user.getUsername(),
                user.getRoles().stream().map(Role::getName).collect(Collectors.toList()));

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @GetMapping("/test-password")
    public ResponseEntity<?> testPassword(@RequestParam String password, @RequestParam String hash) {
        boolean matches = encoder.matches(password, hash);
        logger.info("Password test: password={}, hash={}, matches={}", password, hash, matches);
        return ResponseEntity.ok(new MessageResponse("Password matches: " + matches));
    }

    @GetMapping("/oauth2/callback/google")
    public void googleAuthCallback(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("Google auth callback called");
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            logger.debug("Authentication object: {}", authentication);
            if (authentication == null) {
                logger.error("Authentication is null in SecurityContextHolder");
                response.setContentType("text/html");
                response.getWriter().write("<script>window.opener.location.href='        config.addAllowedOrigin(\"https://assessmatefinal-6cog.vercel.app\");\r\n" + //
                        "/login?error=Authentication+is+null';setTimeout(() => window.close(), 200);</script>");
                return;
            }
            if (!(authentication instanceof OAuth2AuthenticationToken)) {
                logger.error("Authentication is not an OAuth2AuthenticationToken: {}", authentication.getClass().getName());
                response.setContentType("text/html");
                response.getWriter().write("<script>window.opener.location.href='        config.addAllowedOrigin(\"https://assessmatefinal-6cog.vercel.app\");\r\n" + //
                        "/login?error=Invalid+token+type';setTimeout(() => window.close(), 200);</script>");
                return;
            }

            OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
            Map<String, Object> attributes = token.getPrincipal().getAttributes();
            String email = (String) attributes.get("email");
            String fullName = (String) attributes.get("name");
            String username = email.split("@")[0];

            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> {
                        User newUser = new User();
                        newUser.setUsername(username);
                        newUser.setEmail(email);
                        newUser.setFullName(fullName);
                        newUser.setPassword(encoder.encode("oauth2-user-" + System.currentTimeMillis()));
                        newUser.setRoles(new HashSet<>()); // No roles assigned initially
                        return userRepository.save(newUser);
                    });

            UserDetailsImpl userDetails = UserDetailsImpl.build(user);
            Authentication newAuthentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(newAuthentication);

            String jwt = jwtUtils.generateJwtToken(newAuthentication);
            logger.info("JWT generated for OAuth2 user: {}", userDetails.getUsername());

            boolean needsRoleSelection = user.getRoles().isEmpty();

            response.setContentType("text/html");
            response.getWriter().write(
                    "<script>" +
                            "window.opener.location.href='        config.addAllowedOrigin(\"https://assessmatefinal-6cog.vercel.app\");\r\n" + //
                            "/auth/callback?token=" + jwt + "&needsRoleSelection=" + needsRoleSelection + "';" +
                            "setTimeout(() => window.close(), 200);" +
                            "</script>"
            );
        } catch (Exception e) {
            logger.error("OAuth2 authentication error: {}", e.getMessage(), e);
            response.setContentType("text/html");
            response.getWriter().write("<script>window.opener.location.href='        config.addAllowedOrigin(\"https://assessmatefinal-6cog.vercel.app\");\r\n" + //
                    "/login?error=OAuth2+authentication+failed';setTimeout(() => window.close(), 200);</script>");
        }
    }

    @PostMapping("/set-role")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> setUserRole(@RequestBody RoleRequest roleRequest) {
        logger.info("Setting role for user: {}", SecurityContextHolder.getContext().getAuthentication().getName());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getRoles().isEmpty()) {
            logger.warn("User {} already has roles: {}", username,
                    user.getRoles().stream().map(Role::getName).collect(Collectors.toList()));
            return ResponseEntity.badRequest().body(new MessageResponse("User already has a role assigned"));
        }

        String roleInput = roleRequest.getRole().toUpperCase();
        if (!roleInput.equals("STUDENT") && !roleInput.equals("TEACHER")) {
            logger.error("Invalid role provided: {}", roleRequest.getRole());
            return ResponseEntity.badRequest().body(new MessageResponse("Invalid role: " + roleRequest.getRole()));
        }

        ERole roleEnum = ERole.valueOf("ROLE_" + roleInput);
        Role role = roleRepository.findByName(roleEnum)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleEnum));

        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);

        userRepository.save(user);
        logger.info("Role {} assigned to user: {}", roleEnum, username);

        // Generate a new JWT with updated roles
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        Authentication newAuthentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        String newJwt = jwtUtils.generateJwtToken(newAuthentication);

        List<String> updatedRoles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(newJwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                userDetails.getFullName(),
                updatedRoles));
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Invalid or missing token"));
        }

        String jwt = authHeader.substring(7);
        if (!jwtUtils.validateJwtToken(jwt)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Invalid token"));
        }

        String username = jwtUtils.getUserNameFromJwtToken(jwt);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                userDetails.getFullName(),
                roles));
    }

    // Inner class for role request
    private static class RoleRequest {
        private String role;

        public String getRole() {
            return role;
        }
    }
}