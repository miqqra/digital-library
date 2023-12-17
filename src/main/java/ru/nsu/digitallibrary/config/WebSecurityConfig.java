package ru.nsu.digitallibrary.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ru.nsu.digitallibrary.config.filter.CustomAuthFilter;
import ru.nsu.digitallibrary.config.filter.CustomAuthorizationFilter;
import ru.nsu.digitallibrary.model.Roles;
import ru.nsu.digitallibrary.service.security.AppUserService;

@EnableWebSecurity
@Configuration
@EnableMethodSecurity
public class WebSecurityConfig {

    private final AccountAuthenticationProvider authenticationProvider;
    private final AppUserService appUserService;

    public WebSecurityConfig(AccountAuthenticationProvider authenticationProvider, AppUserService appUserService) {
        this.authenticationProvider = authenticationProvider;
        this.appUserService = appUserService;
    }

    @Value("${security.type}")
    private String security;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        if (security.equals("token")) {
            CustomAuthFilter filter = new CustomAuthFilter(authenticationProvider, appUserService);
            filter.setFilterProcessesUrl("/login");
            http.csrf().disable();
            http.authorizeHttpRequests()
                    .antMatchers("/accounts/token/refresh", "/accounts/token/info").permitAll()
                    .antMatchers("/**/root/**").hasRole(Roles._ROOT)
                    .antMatchers("/**/admin/**").hasRole(Roles._ADMIN)
                    .antMatchers("/**/user/**").hasRole(Roles._USER)
                    .antMatchers("/**").hasRole(Roles._USER);
            http
//                .antMatcher("/log")
//                .authenticationProvider(authenticationProvider)
//                .httpBasic(withDefaults())
                    .sessionManagement()
                    .sessionCreationPolicy(STATELESS);
            http.addFilter(filter);
            http.addFilterBefore(new CustomAuthorizationFilter(appUserService), UsernamePasswordAuthenticationFilter.class);
        } else if (security.equals("no")) {
            http.csrf().disable().sessionManagement()
                    .sessionCreationPolicy(STATELESS);
            http.authorizeRequests().antMatchers("/**").permitAll();
        } else {
            http.csrf().disable();
            http.authorizeRequests().antMatchers("/**").hasRole("USER").and().httpBasic();
            http.sessionManagement()
                    .sessionCreationPolicy(STATELESS);
        }
        return http.build();
    }
}
