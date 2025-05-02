package edu.cit.AssessMate.Repository;

import edu.cit.AssessMate.Model.ERole;
import edu.cit.AssessMate.Model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(ERole name);
}
