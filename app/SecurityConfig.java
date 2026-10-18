package app;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMINISTRACION")
                .requestMatchers("/area-producto/**").hasRole("AREA_PRODUCTO")
                .requestMatchers("/area-ventas/**").hasRole("AREA_VENTAS")
                .requestMatchers("/gerencia/**").hasRole("GERENCIA")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            );
        return http.build();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsManager() {
        User admin = User.builder()
            .username("admin")
            .password("{noop}admin123")
            .roles("ADMINISTRACION")
            .build();

        User producto = User.builder()
            .username("producto")
            .password("{noop}producto123")
            .roles("AREA_PRODUCTO")
            .build();

        User ventas = User.builder()
            .username("ventas")
            .password("{noop}ventas123")
            .roles("AREA_VENTAS")
            .build();

        User gerencia = User.builder()
            .username("gerencia")
            .password("{noop}gerencia123")
            .roles("GERENCIA")
            .build();

        return new InMemoryUserDetailsManager(admin, producto, ventas, gerencia);
    }
}
