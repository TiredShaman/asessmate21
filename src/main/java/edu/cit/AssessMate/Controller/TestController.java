// package edu.cit.AssessMate.Controller;

// import edu.cit.AssessMate.payload.request.LoginRequest;
// import edu.cit.AssessMate.payload.response.MessageResponse;
// import lombok.RequiredArgsConstructor;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.authentication.AuthenticationManager;
// import org.springframework.security.authentication.BadCredentialsException;
// import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.web.bind.annotation.*;

// @RestController
// @RequestMapping("/api/test")
// @RequiredArgsConstructor
// public class TestController {
//     private static final Logger logger = LoggerFactory.getLogger(TestController.class);
//     private final AuthenticationManager authenticationManager;

//     @PostMapping("/auth-test")
//     public ResponseEntity<?> testAuth(@RequestBody LoginRequest loginRequest) {
//         logger.info("Test auth attempt for user: {}", loginRequest.getUsername());
//         try {
//             Authentication auth = authenticationManager.authenticate(
//                     new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
//             logger.info("Test auth successful for user: {}", loginRequest.getUsername());
//             return ResponseEntity.ok(new MessageResponse("Authentication successful: " + auth.getName()));
//         } catch (BadCredentialsException e) {
//             logger.error("Invalid credentials for user: {}", loginRequest.getUsername());
//             return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                     .body(new MessageResponse("Invalid credentials"));
//         } catch (Exception e) {
//             logger.error("Test auth error: {}", e.getMessage(), e);
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                     .body(new MessageResponse("Error: " + e.getMessage()));
//         }
//     }
// }