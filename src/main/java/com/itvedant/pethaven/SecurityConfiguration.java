package com.itvedant.pethaven;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Public
                .requestMatchers("/users/register", "/error").permitAll()

                // Login (needs email + password)
                .requestMatchers("/users/login").authenticated()

                // View animals and categories: USER and ADMIN
                .requestMatchers(HttpMethod.GET, "/animals/**", "/categories/**").hasAnyRole("USER", "ADMIN")

                // Add / update / delete animals and categories: ADMIN only
                .requestMatchers("/animals/**", "/categories/**").hasRole("ADMIN")

                // Send request and view own requests: USER only
                .requestMatchers("/adoptions/request/**", "/adoptions/my").hasRole("USER")

                // View all, approve, reject: ADMIN only
                .requestMatchers("/adoptions/**").hasRole("ADMIN")

                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}