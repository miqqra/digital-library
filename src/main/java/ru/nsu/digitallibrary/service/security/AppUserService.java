package ru.nsu.digitallibrary.service.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.nsu.digitallibrary.entity.security.AppUser;
import ru.nsu.digitallibrary.entity.security.Role;
import ru.nsu.digitallibrary.exception.ClientException;
import ru.nsu.digitallibrary.model.Roles;
import ru.nsu.digitallibrary.repository.security.AppUserRepository;
import ru.nsu.digitallibrary.repository.security.RoleRepository;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AppUserService {
    private final AppUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final WeakHashMap<String, String> aTokenMap = new WeakHashMap<>();
    private final WeakHashMap<String, AppUser> userCache = new WeakHashMap<>();


    public Role saveRole(Role role) {
        Roles.greaterPermission(role.getName());
        log.info("Save new role with name {}", role.getName());
        return roleRepository.save(role);
    }

    public AppUser createAppUser(String userName, String password, Collection<Role> roles) {
        if (roles == null) {
            roles = new ArrayList<>();
        }
        return createAppUser(userName, password, roles.stream().map(Role::getName).collect(Collectors.toSet()));
    }

    public AppUser createAppUser(String userName, String password, Set<String> roles) {
        if (roles == null) {
            roles = new HashSet<>();
        }
        Roles.greaterPermission(roles);
        checkPassword(password);
        checkUserName(userName);
        log.info("Adding new user {} with {}", userName, roles);
        List<String> notExists = new ArrayList<>();
        Set<Role> exists = new HashSet<>();
        for (String role : roles) {
            var foundedRole = roleRepository.findByName(role);
            foundedRole.ifPresent(exists::add);
            if (foundedRole.isEmpty()) {
                notExists.add(role);
            }
        }
        if (!notExists.isEmpty()) {
            log.error("for {}: Roles not exists {}", userName, notExists);
            throw ClientException.of(HttpStatus.BAD_REQUEST, "Roles not found: " + notExists);
        }
        AppUser newUser = new AppUser();
        newUser.setRoles(exists);
        newUser.setUsername(userName);
        newUser.setPassword(encoder.encode(password));
        userCache.put(userName, newUser);
        log.info("Added new user {} with roles {}", userName, roles);
        return userRepository.save(newUser);
    }

    public AppUser createAppUser(String userName, String password) {
        return createAppUser(userName, password, (Set<String>) null);
    }

    public AppUser createAppUser(AppUser appUser) {
        return createAppUser(appUser.getUsername(), appUser.getPassword(), (List<Role>) null);
    }

    public AppUser addRoleToUser(String userName, String roleName) {
        checkUserName(userName);
        Roles.greaterPermission(roleName);
        log.info("Start adding role {} to user {}", roleName, userName);

        var user = findUser(userName);
        if (user.isEmpty()) {
            log.warn("User {} not found", userName);
            throw ClientException.of(HttpStatus.NOT_FOUND, "User not found: " + userName);
        }
        var role = roleRepository.findByName(roleName);
        if (role.isEmpty()) {
            log.warn("Role {} not found", roleName);
            throw ClientException.of(HttpStatus.NOT_FOUND, "Role not found: " + roleName);
        }
        log.info("Added role {} to user {}", roleName, userName);
        user.get().getRoles().add(role.get());
        user.get().setAccess_token("");
        aTokenMap.remove(user.get().getUsername());
        return user.get();
    }

    public AppUser deleteRoleFromUser(String userName, String roleName) {
        checkUserName(userName);
        Roles.greaterPermission(roleName);
        log.info("Start delete role {} from user {}", roleName, userName);
        var user = findUser(userName);
        if (user.isEmpty()) {
            log.warn("User {} not found", userName);
            throw ClientException.of(HttpStatus.NOT_FOUND, "User not found: " + userName);
        }
        var role = roleRepository.findByName(roleName);
        if (role.isEmpty()) {
            log.warn("Role {} not found", roleName);
            throw ClientException.of(HttpStatus.NOT_FOUND, "Role not found: " + roleName);
        }
        var userR = user.get();
        userR.getRoles().remove(role.get());
        userR.setAccess_token("");
        aTokenMap.remove(userR.getUsername());
        log.info("Deleted role {} from user {}", roleName, userName);
        return userR;
    }

    public AppUser getUser(String username) throws ClientException {
        log.info("Getting user {}", username);
        var user = findUser(username);
        if (user.isPresent()) {
            return user.get();
        } else {
            throw ClientException.of(HttpStatus.NOT_FOUND, "User " + username + " not found");
        }
    }

    public List<AppUser> getUsers() {
        Roles.mustBeAdmin();
        log.info("Get users {}", SecurityContextHolder.getContext().getAuthentication().getAuthorities());
        return userRepository.findAll();
    }

    public List<Role> getRoles() {
        log.info("Get all roles");
        return roleRepository.findAll();
    }


    public static void checkUserName(String username) {
        if (username == null || username.isBlank()) {
            log.warn("Null or empty username");
            throw ClientException.of(HttpStatus.BAD_REQUEST, "There is no username");
        }
        if (!username.matches("(\\w)+")) {
            log.warn("Bad username {}", username);
            throw ClientException.of(HttpStatus.BAD_REQUEST, "Bad username. Username must contains" +
                    "only digits letters");
        }
    }

    public static void checkPassword(String password) {
        if (password == null || password.isBlank()) {
            log.warn("Null password in checking");
            throw ClientException.of(HttpStatus.BAD_REQUEST, "There is no password");
        }
        if (!password.matches("(\\S)+")) {
//            log.error("Bad password {}", password);
            throw ClientException.of(HttpStatus.BAD_REQUEST, "Bad password. Password must contains" +
                    "only digits letters and signs");
        }
    }

    public static void checkUser(AppUser user) {
        if (user.getRoles() == null) {
            user.setRoles(new HashSet<>());
        }
        Roles.greaterPermission(user.getRoles().stream().map(Role::toString).collect(Collectors.toList()));
        checkUserName(user.getUsername());
        checkPassword(user.getPassword());
    }

    public static void checkRole(Role role) {
        if (role == null) {
            log.warn("Null role in checking");
            throw ClientException.of(HttpStatus.BAD_REQUEST, "There is no role");
        }
        if (!role.getName().matches("(\\S)+")) {
            log.warn("Bad role name {}", role.getName());
            throw ClientException.of(HttpStatus.BAD_REQUEST, "Bad roleName. roleName must contains" +
                    "only digits letters");
        }

    }

    public void deleteRole(String roleName) {
        Roles.greaterPermission(roleName);
        if (Roles.isBasicRole(roleName)) {
            log.warn("Trying to delete basic roles");
            ClientException.of(HttpStatus.BAD_REQUEST, "DONT DELETE BASIC ROLES");
        }
        if (!roleExists(roleName)) {
            log.warn("No role {} for delete", roleName);
            ClientException.of(HttpStatus.BAD_REQUEST, "There is no role in data");
        }
        Role role = roleRepository.findByName(roleName).get();
        for (AppUser user : getUsers()) {
            var roles = user.getRoles();
            if (roles.contains(role)) {
                user.removeRole(role);
                user.setAccess_token("");
                aTokenMap.remove(user.getUsername());

            }

        }
        roleRepository.deleteByName(roleName);
        log.info("Deleted role {}", roleName);
    }

    public void deleteUser(String username) {
        if (username == null) {
            throw ClientException.of(HttpStatus.BAD_REQUEST, "There is no username");
        }
        aTokenMap.remove(username);
        AppUser appUser = getUser(username);
        Roles.greaterPermission(appUser.getRoles());
        appUser.getRoles().clear();
        userCache.remove(username);
        userRepository.deleteByUsername(username);
        log.info("Deleted user {}", username);
    }

    public boolean roleExists(Role role) {
        return roleExists(role.getName());
    }

    public boolean roleExists(String roleName) {
        return roleRepository.findByName(roleName).isPresent();
    }

    public AppUser changePassword(String username, String newPassword) {
        AppUser user = getUser(username);
        Roles.greaterPermission(user.getRoles());
        checkPassword(newPassword);
        user.setPassword(encoder.encode(newPassword));
        return user;
    }

    @Transactional
    public void updateRefreshToken(String username, String refreshToken) {
        var user = getUser(username);
        user.setRefresh_token(refreshToken);
        log.info("refresh token {}", refreshToken);
        log.info("Refresh token updated {}-{}", username, refreshToken);
    }

    @Transactional
    public void updateAccessToken(String username, String accessToken) {
        var user = getUser(username);

        user.setAccess_token(accessToken);
        log.info("access token {}", accessToken);
        aTokenMap.put(username, accessToken);
        log.info("Access token updated {}-{}", username, accessToken);
//        saveUser(user);

    }

    public String getRefreshToken(String username) {
        log.warn("get user refresh: {}", getUser(username));
        try {
            log.warn("get user {} accessToken", username);
            return getUser(username).getRefresh_token();
        } catch (ClientException e) {
            log.warn("user not found {}", username);
            throw ClientException.of(HttpStatus.NOT_FOUND, "User not found");
        }
    }

    public String getAccessToken(String username) {
        log.info("user accessToken {}", username);
        String token = aTokenMap.get(username);
        if (token != null) {
            return token;
        }
        try {
            log.warn("get user {} accessToken", username);
            return getUser(username).getAccess_token();
        } catch (ClientException e) {
            log.warn("user not found {}", username);
            throw ClientException.of(HttpStatus.NOT_FOUND, "User not found");
        }
    }

    private Optional<AppUser> findUser(String userName) {
        AppUser userTmp = userCache.get(userName);
        Optional<AppUser> user;
        if (userTmp != null) {
            user = Optional.of(userTmp);
        } else {
            user = userRepository.findByUsername(userName);
        }
        return user;
    }

    public void clearCache() {
        aTokenMap.clear();
        userCache.clear();
    }

}
