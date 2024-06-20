package ru.nsu.digitallibrary.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.security.Principal;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.nsu.digitallibrary.dto.LoginUserDto;
import ru.nsu.digitallibrary.dto.RegistrateUserDto;
import ru.nsu.digitallibrary.entity.postgres.UserCredentials;
import ru.nsu.digitallibrary.exception.ClientException;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCredentialsService {

    private final UserDetailsServiceImpl userDetailsService;

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public static String getCurrentUser() {
        return Optional.of(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .map(Principal::getName)
                .orElseThrow(() -> ClientException.of(HttpStatus.FORBIDDEN, "Клиент не авторизован"));
    }

    public boolean registerUser(RegistrateUserDto dto, HttpServletRequest httpServletRequest) {
        UserCredentials userCredentials = new UserCredentials()
                .setPassword(dto.getPassword())
                .setUsername(dto.getNickname());

        userDetailsService.saveUser(userCredentials);

        authenticate(userCredentials, dto.getPassword(), httpServletRequest);

        return true;
    }

    public boolean loginUser(LoginUserDto dto, HttpServletRequest httpServletRequest) {
        UserDetails userCredentials = userDetailsService.loadUserByUsername(dto.getLogin());

        if (Objects.isNull(userCredentials)
                || passwordEncoder.matches(userCredentials.getPassword(), dto.getPassword())) {
            throw ClientException.of(HttpStatus.NOT_FOUND, "Неправильный логин или пароль");
        }

        authenticate(userCredentials, dto.getPassword(), httpServletRequest);

        return true;
    }

    public void logoutUser(HttpServletRequest httpServletRequest) {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(null);
        SecurityContextHolder.getContext().setAuthentication(null);
        HttpSession httpSession = httpServletRequest.getSession();
        httpSession.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);
    }

    private void authenticate(UserDetails userCredentials, String password, HttpServletRequest httpServletRequest) {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(
                        userCredentials,
                        password,
                        userCredentials.getAuthorities()
                );

        Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);

        if (usernamePasswordAuthenticationToken.isAuthenticated()) {
            SecurityContext securityContext = SecurityContextHolder.getContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            HttpSession httpSession = httpServletRequest.getSession(true);
            httpSession.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);
        }
    }
}

