package io.everyonecodes.cryptolog.repository;

import io.everyonecodes.cryptolog.data.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<List<User>> findByStatus(String status);

    Optional<User> findUserByResetToken(String token);
}
