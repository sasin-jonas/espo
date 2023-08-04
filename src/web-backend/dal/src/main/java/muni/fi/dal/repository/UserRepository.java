package muni.fi.dal.repository;

import muni.fi.dal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByJwtIdentifier(String jwtIdentifier);

    List<User> findAllByRolesName(String roleName);
}
