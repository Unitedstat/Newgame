package com.javaproject.security;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final DataSource dataSource;
    private final LoggingAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(DataSource dataSource,
                          LoggingAccessDeniedHandler accessDeniedHandler) {
        this.dataSource = dataSource;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JdbcUserDetailsManager jdbcUserDetailsManager() {
        JdbcUserDetailsManager manager = new JdbcUserDetailsManager();
        manager.setDataSource(dataSource);
        return manager;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/user/**").hasAnyRole("USER","MANAGER")
                .requestMatchers("/secured/**").hasAnyRole("USER","MANAGER")
                .requestMatchers("/manager/**").hasRole("MANAGER")
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/", "/**").permitAll()
            )
            .formLogin(login -> login
                .loginPage("/login")
                .defaultSuccessUrl("/secured")
            )
            .logout(logout -> logout
                .invalidateHttpSession(true)
                .clearAuthentication(true)
            )
            .exceptionHandling(ex -> ex
                .accessDeniedHandler(accessDeniedHandler)
            );

        http.csrf(csrf -> csrf.disable());
        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }
}
