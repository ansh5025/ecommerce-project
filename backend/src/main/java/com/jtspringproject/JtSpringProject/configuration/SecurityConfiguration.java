package com.jtspringproject.JtSpringProject.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jtspringproject.JtSpringProject.models.User;
import com.jtspringproject.JtSpringProject.services.userService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Configuration
public class SecurityConfiguration {

    private final userService userService;

    public SecurityConfiguration(userService userService) {
        this.userService = userService;
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // --- API security chain (highest priority) ---
    @Configuration
    @Order(1)
    public static class ApiSecurityAdapter {

        private final ObjectMapper objectMapper = new ObjectMapper();

        @Bean
        SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
            http.antMatcher("/api/**")
                    .csrf().disable()
                    .authorizeHttpRequests(requests -> requests
                            .antMatchers("/api/auth/login", "/api/auth/logout", "/api/users/register").permitAll()
                            .antMatchers("/api/admin/**").hasRole("ADMIN")
                            .antMatchers("/api/**").hasAnyRole("USER", "ADMIN", "NORMAL"))
                    .exceptionHandling(ex -> ex
                            .authenticationEntryPoint((request, response, e) -> {
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                response.setContentType("application/json");
                                objectMapper.writeValue(response.getWriter(),
                                        Map.of("message", "Authentication required"));
                            })
                            .accessDeniedHandler((request, response, e) -> {
                                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                response.setContentType("application/json");
                                objectMapper.writeValue(response.getWriter(),
                                        Map.of("message", "Access denied"));
                            }));
            return http.build();
        }
    }

    // --- Admin MVC chain ---
    @Configuration
    @Order(2)
    public static class AdminConfigurationAdapter {

        @Bean
        SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
            http.antMatcher("/admin/**")
                    .authorizeHttpRequests(requests -> requests
                            .requestMatchers(new AntPathRequestMatcher("/admin/login")).permitAll()
                            .requestMatchers(new AntPathRequestMatcher("/admin/**")).hasRole("ADMIN"))
                    .formLogin(login -> login
                            .loginPage("/admin/login")
                            .loginProcessingUrl("/admin/loginvalidate")
                            .successHandler((request, response, authentication) -> {
                                response.sendRedirect("/admin/");
                            })
                            .failureHandler((request, response, exception) -> {
                                response.sendRedirect("/admin/login?error=true");
                            }))
                    .logout(logout -> logout.logoutRequestMatcher(new AntPathRequestMatcher("/admin/logout", "GET"))
                            .logoutSuccessUrl("/admin/login")
                            .deleteCookies("JSESSIONID"))
                    .exceptionHandling(exception -> exception
                            .accessDeniedHandler((request, response, accessDeniedException) -> {
                                response.sendRedirect("/admin/login?error=true");
                            }));
            return http.build();
        }
    }

    // --- User MVC chain ---
    @Configuration
    @Order(3)
    public static class UserConfigurationAdapter {

        @Bean
        SecurityFilterChain userFilterChain(HttpSecurity http) throws Exception {
            http.authorizeHttpRequests(requests -> requests
                    .antMatchers("/login", "/register", "/newuserregister", "/css/**", "/images/**", "/webjars/**").permitAll()
                    .antMatchers("/**").hasRole("USER"))
                    .formLogin(login -> login
                            .loginPage("/login")
                            .loginProcessingUrl("/userloginvalidate")
                            .successHandler((request, response, authentication) -> {
                                response.sendRedirect("/");
                            })
                            .failureHandler((request, response, exception) -> {
                                response.sendRedirect("/login?error=true");
                            }))
                    .logout(logout -> logout.logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                            .logoutSuccessUrl("/login")
                            .deleteCookies("JSESSIONID"))
                    .exceptionHandling(exception -> exception
                            .accessDeniedPage("/403"));
            return http.build();
        }
    }

    @Bean
    UserDetailsService userDetailsService() {
        return username -> {
            User user = userService.getUserByUsername(username);
            if (user == null) {
                throw new UsernameNotFoundException("User with username " + username + " not found.");
            }
            String role = "ROLE_ADMIN".equals(user.getRole()) ? "ADMIN" : "USER";

            return org.springframework.security.core.userdetails.User
                    .withUsername(username)
                    .password(user.getPassword())
                    .roles(role)
                    .build();
        };
    }
}
