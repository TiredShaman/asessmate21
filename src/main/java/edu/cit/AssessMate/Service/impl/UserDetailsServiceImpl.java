package edu.cit.AssessMate.Service.impl;

import edu.cit.AssessMate.Model.User; // Assuming your User entity class
import edu.cit.AssessMate.Repository.UserRepository; // Assuming your UserRepository interface
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Collectors;


/**
 * Custom implementation of Spring Security's UserDetailsService.
 * Loads user details from the database.
 */
@Service
@RequiredArgsConstructor // Lombok annotation for constructor injection
public class UserDetailsServiceImpl implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    // UserRepository injected by Lombok
    private final UserRepository userRepository;

    /**
     * Loads user details by username.
     * This method is called by Spring Security during authentication.
     *
     * @param username The username of the user to load.
     * @return UserDetails object representing the user.
     * @throws UsernameNotFoundException if the user is not found.
     */
    @Override
    @Transactional // Ensure the operation is transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("Attempting to load user: {}", username);

        // Find the user by username in the database
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("User not found: {}", username);
                    return new UsernameNotFoundException("User Not Found with username: " + username);
                });

        // Log the user found and their roles
        logger.info("User found: {}, roles: {}", user.getUsername(),
                user.getRoles().stream().map(role -> role.getName().name()).collect(Collectors.toList()));
        // Log password hash (be cautious in production logs)
        logger.debug("User password hash: {}", user.getPassword());

        // Build and return a UserDetails object from the User entity
        // This UserDetailsImpl.build() method is CRITICAL for mapping database roles to Spring Security authorities.
        return UserDetailsImpl.build(user);
    }
}
