package ru.nsu.digitallibrary.service.data;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nsu.digitallibrary.entity.postgres.UserCredentials;
import ru.nsu.digitallibrary.repository.postgres.RoleRepository;
import ru.nsu.digitallibrary.repository.postgres.UserCredentialsRepository;

@Service
@RequiredArgsConstructor
public class UserCredentialsDataService {

    private final UserCredentialsRepository userCredentialsRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return Optional.of(username)
                .map(userCredentialsRepository::existsByUsername)
                .orElse(true);
    }

    @Transactional(readOnly = true)
    public UserDetails findByUsername(String username) {
        return Optional.of(username)
                .map(userCredentialsRepository::findByUsername)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public UserCredentials findById(Long id) {
        return Optional.of(id)
                .flatMap(userCredentialsRepository::findById)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<UserCredentials> findAll() {
        return userCredentialsRepository.findAll();
    }

    @Transactional
    public UserCredentials saveUser(UserCredentials userCredentials) {
        return Optional.of(userCredentials)
                .map(user -> {
                    setDefaultRole(userCredentials);
                    user.setPassword(passwordEncoder.encode(user.getPassword()));
                    return user;
                })
                .map(userCredentialsRepository::save)
                .orElse(null);
    }

    private void setDefaultRole(UserCredentials userCredentials) {
        userCredentials.setRoles(Collections.emptySet()); //todo
    }

    public void deleteUser(Long id) {
        userCredentialsRepository.deleteById(id);
    }
}

