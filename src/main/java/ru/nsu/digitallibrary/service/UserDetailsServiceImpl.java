package ru.nsu.digitallibrary.service;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.nsu.digitallibrary.entity.postgres.UserCredentials;
import ru.nsu.digitallibrary.exception.ClientException;
import ru.nsu.digitallibrary.service.data.UserCredentialsDataService;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserCredentialsDataService dataService;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return Optional.of(username)
                .map(dataService::findByUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
    }

    public UserCredentials findUserById(Long userId) {
        return Optional.of(userId)
                .map(dataService::findById)
                .orElseThrow(() -> ClientException.of(HttpStatus.NOT_FOUND, "Пользователь не найден"));
    }

    public List<UserCredentials> allUsers() {
        return dataService.findAll();
    }

    public boolean saveUser(UserCredentials user) {
        validateUserExists(user);

        return Optional.of(user)
                .map(dataService::saveUser)
                .isPresent();
    }

    public void deleteUser(Long userId) {
        validateUserNotExists(userId);
        dataService.deleteUser(userId);
    }

    private void validateUserExists(UserCredentials userCredentials) {
        Optional.of(userCredentials)
                .map(UserCredentials::getUsername)
                .map(dataService::findByUsername)
                .ifPresent(v -> {
                    throw ClientException.of(HttpStatus.BAD_REQUEST, "Пользователь с таким именем уже существует");
                });
    }

    private void validateUserNotExists(Long id) {
        Optional.of(id)
                .map(dataService::findById)
                .orElseThrow(() -> ClientException.of(HttpStatus.BAD_REQUEST, "Такого пользователя не существует"));
    }
}

