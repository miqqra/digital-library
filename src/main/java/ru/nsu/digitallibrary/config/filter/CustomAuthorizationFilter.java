package ru.nsu.digitallibrary.config.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.nsu.digitallibrary.config.CustomSecurityConfig;
import ru.nsu.digitallibrary.exception.ClientException;
import ru.nsu.digitallibrary.service.security.AppUserService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class CustomAuthorizationFilter extends OncePerRequestFilter {

    private final AppUserService appUserService;

    public CustomAuthorizationFilter(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    private final String secretWord = CustomSecurityConfig.secretWord;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.debug("operation {}", request.getServletPath());
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, PATCH, HEAD");
        response.addHeader("Access-Control-Allow-Headers", "username, password, content-type, Origin, Authorization, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers");
        response.addHeader("Access-Control-Expose-Headers", "Access-Control-Allow-Origin, Access-Control-Allow-Credentials");
        response.addHeader("Access-Control-Allow-Credentials", "true");
        response.addIntHeader("Access-Control-Max-Age", 10);

        if (request.getMethod().equals("OPTIONS")) {
            response.setStatus(200);
            return;
        }

        if (request.getServletPath().equals("/login") || request.getServletPath().equals("/accounts/token/refresh")) {
            log.info("Login {}", request.getHeader("username"));
            filterChain.doFilter(request, response);
        } else {
            String authorizationHeader = request.getHeader(AUTHORIZATION);
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                try {
                    String token = authorizationHeader.substring("Bearer ".length());
                    Algorithm algorithm = Algorithm.HMAC256(secretWord.getBytes());
                    JWTVerifier verifier = JWT.require(algorithm).build();
                    DecodedJWT decodedJWT = verifier.verify(token);
                    String username = decodedJWT.getSubject();
                    String oldToken = appUserService.getAccessToken(username);
                    if (oldToken == null) {
                        log.warn("There is no access token for {}", username);
                        throw ClientException.of(HttpStatus.UNAUTHORIZED, "There is no access token for you");
                    }
                    if (oldToken.isEmpty()) {
                        log.warn("There is no access token for {}", username);
                        throw ClientException.of(HttpStatus.UNAUTHORIZED, "Refresh your token");
                    }

                    if (!oldToken.equals(token)) {
                        log.warn("It's not current access token {}", username);
                        throw ClientException.of(HttpStatus.UNAUTHORIZED, "It's not current access token");
                    }

                    String[] roles = decodedJWT.getClaim("roles").asArray(String.class);
                    Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    Arrays.stream(roles).forEach(role ->
                            authorities.add(new SimpleGrantedAuthority(role)));
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(username, authorities, authorities);

                    var copy = SecurityContextHolder.getContext();
                    copy.setAuthentication(authenticationToken);
                    SecurityContextHolder.setContext(copy);
                    filterChain.doFilter(request, response);
                    log.info("User {} authorized", username);
                } catch (ClientException e) {
                    log.warn("Response exception {}", e.response());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setStatus(e.getCode().value());
                    new ObjectMapper().writeValue(response.getOutputStream(),
                            new ClientException.Response(e.getCode(), e.getMessage()));

                } catch (TokenExpiredException e) {
                    log.warn("Token expired {}", request.getHeader(AUTHORIZATION).substring("Bearer ".length()));
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    new ObjectMapper().writeValue(response.getOutputStream(),
                            Map.of("message", "try to refresh token"));
                } catch (Exception e) {
                    log.error("Error logging in {}", e.getMessage());
                    response.setHeader("error", e.getMessage());
                    response.setStatus(HttpStatus.FORBIDDEN.value());

                    Map<String, String> error = new HashMap<>();
                    error.put("error_message", e.getMessage());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    new ObjectMapper().writeValue(response.getOutputStream(),
                            error);
                }
            } else {
                log.info("NOT TOKEN AUTHENTICATION");
                filterChain.doFilter(request, response);
            }


        }
    }
}

