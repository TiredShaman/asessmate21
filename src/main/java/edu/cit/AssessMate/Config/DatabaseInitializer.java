package edu.cit.AssessMate.Config;

import edu.cit.AssessMate.Model.ERole;
import edu.cit.AssessMate.Model.Role;
import edu.cit.AssessMate.Repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class DatabaseInitializer {

    @Autowired
    private RoleRepository roleRepository;

    @PostConstruct
    public void init() {
        if (roleRepository.findByName(ERole.ROLE_STUDENT).isEmpty()) {
            roleRepository.save(new Role(ERole.ROLE_STUDENT));
        }
        if (roleRepository.findByName(ERole.ROLE_TEACHER).isEmpty()) {
            roleRepository.save(new Role(ERole.ROLE_TEACHER));
        }
    }
}