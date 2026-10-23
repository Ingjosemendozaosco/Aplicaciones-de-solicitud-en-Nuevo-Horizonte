package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**", "/js/**", "/images/**", "/webjars/**", "/error").permitAll()
                .requestMatchers("/usuarios/**", "/roles/**", "/documentos/**").hasRole("Gerencia")
                .requestMatchers("/disponibles", "/disponibles/**")
                    .hasAnyRole("Administración", "Gerencia")
                .requestMatchers("/reportes/**").hasAnyRole("Administración", "Gerencia")
                .requestMatchers(HttpMethod.GET, "/solicitudes/nueva")
                    .hasAnyRole("Area de Producto", "Area de Ventas")
                .requestMatchers(HttpMethod.POST, "/solicitudes")
                    .hasAnyRole("Area de Producto", "Area de Ventas")
                .requestMatchers(HttpMethod.POST, "/solicitudes/*/decision")
                    .hasRole("Administración")
                .requestMatchers(HttpMethod.POST, "/solicitudes/*/modulos/**")
                    .hasRole("Administración")
                .requestMatchers(HttpMethod.POST, "/solicitudes/*/propuesta")
                    .hasRole("Administración")
                .requestMatchers("/solicitudes/**", "/dashboard").authenticated()
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
