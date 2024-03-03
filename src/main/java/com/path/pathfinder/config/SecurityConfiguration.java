package com.path.pathfinder.config;

import com.path.pathfinder.mapper.MapStructMapper;
import com.path.pathfinder.model.enumerated.UserRoleEnum;
import com.path.pathfinder.repository.UserRepository;
import com.path.pathfinder.service.PathfinderUserDetailsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
public class SecurityConfiguration {


    private final String rememberMeKey;

    public SecurityConfiguration(@Value("${pathfinder.remember-me-key}") String rememberMeKey) {
        this.rememberMeKey = rememberMeKey;
    }

    @Bean
    public PasswordEncoder encode() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http // .csrf(AbstractHttpConfigurer::disable)
                // defines which pages will be authorized
                .authorizeHttpRequests((auth) -> {
                    auth
                            // allow access to all static locations defined in StaticResourceLocation enum class
                            // (images, css, js, webjars, etc.)
                            .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                            // the URLs below are available for all users - logged in and anonymous
                            .requestMatchers(
                                    "/",
                                    "/index",
                                    "/home",
                                    "/users/logout",
                                    "/users/login",
                                    "/users/register",
                                    "/users/login-error",
                                    "/about").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/**").permitAll()
                            .requestMatchers(HttpMethod.POST, "/api/**").authenticated()
                            .requestMatchers("/superuser/comments/*").hasAnyRole(
                                    UserRoleEnum.ADMIN.name(),
                                    UserRoleEnum.MODERATOR.name()
                            ).requestMatchers("/superuser/permissions/*").hasRole(
                                    UserRoleEnum.ADMIN.name()
                            )
                            .requestMatchers("/routes/add").authenticated()
                            .requestMatchers(HttpMethod.GET, "/routes/**").permitAll()
                            .requestMatchers(HttpMethod.POST, "/routes/**").authenticated()
                            .requestMatchers(HttpMethod.POST, "/images/**").authenticated()
                            .requestMatchers("users/profile/**").authenticated()
                            .anyRequest()
                            .authenticated();
                })
                .formLogin(form -> {
                    form
                            .loginPage("/users/login")
                            .loginProcessingUrl("/users/login")
                            .failureForwardUrl("/users/login-error")
                            // where to go after login (use true arg if we want to go there, otherwise go to prev page)
                            .defaultSuccessUrl("/" /*,true*/) // arg alwaysUse: true
                            // the names of the "username" and "password" input fields in the custom login form
                            .usernameParameter(UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY)
                            .passwordParameter(UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_PASSWORD_KEY)
                            .permitAll();
                })
                .logout(logout -> {
                    logout
                            .logoutUrl("/users/logout")
                            // go to home page after logout
                            .logoutSuccessUrl("/")
                            .deleteCookies("JSESSIONID")
                            .clearAuthentication(true)
                            .invalidateHttpSession(true)
                            .permitAll();
                })
                .securityContext(context -> {
                    context.securityContextRepository(securityContextRepository());
                })
                .rememberMe(mem -> {
                    mem
                            .key(rememberMeKey)
                            .tokenValiditySeconds(3600) // an hour
                            .rememberMeParameter("remember-me-par")
                            .rememberMeCookieName("remember-me-cookie");
                    // https://docs.spring.io/spring-security/reference/servlet/authentication/rememberme.html
                })
                .build();
    }

    @Bean
    UserDetailsService userDetailsService(UserRepository userRepository, MapStructMapper mapper) {
        return new PathfinderUserDetailsService(userRepository, mapper);
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new DelegatingSecurityContextRepository(
                new RequestAttributeSecurityContextRepository(),
                new HttpSessionSecurityContextRepository()
        );
    }
}
