package io.everyonecodes.cryptolog.service;

import io.everyonecodes.cryptolog.data.Role;
import io.everyonecodes.cryptolog.data.User;
import io.everyonecodes.cryptolog.repository.RoleRepository;
import io.everyonecodes.cryptolog.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImp implements UserService {
    private final PasswordEncoder encoder;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public UserServiceImp(PasswordEncoder encoder, RoleRepository roleRepository, UserRepository userRepository) {
        this.encoder = encoder;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void saveUser(User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        user.setStatus("NOT_VERIFIED");
        Role userRole = roleRepository.findByName("SITE_USER");
        if (userRole == null) {
            userRole = new Role("SITE_USER", "This user has access to site, after login - normal user");
        }
        user.setRoles(new HashSet<>(List.of(userRole)));
        userRepository.save(user);
    }

    @Override
    public boolean isUserAlreadyPresent(User user) {
        return userRepository.existsByEmail(user.getEmail());
    }

    @Override
    public Optional<User> findUserByResetToken(String token) {
        return userRepository.findUserByResetToken(token);
    }

    @Override
    public Optional<User> findUserByEmail(String userEmail) {
        return userRepository.findByEmail(userEmail);
    }
}
