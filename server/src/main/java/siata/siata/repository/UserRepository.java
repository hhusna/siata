package siata.siata.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import siata.siata.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
}
