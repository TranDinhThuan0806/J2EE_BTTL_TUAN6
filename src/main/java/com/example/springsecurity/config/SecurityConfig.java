package com.example.springsecurity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Chỉ ADMIN mới được thêm/sửa/xóa sản phẩm
                .requestMatchers("/products/add", "/products/save",
                                 "/products/edit/**", "/products/update/**",
                                 "/products/delete/**").hasRole("ADMIN")
                // Mọi request khác chỉ cần đăng nhập
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .defaultSuccessUrl("/home", true)
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
            )
            // Chuyển hướng đến trang lỗi 403 khi không đủ quyền
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/access-denied")
            );
        return http.build();
    }
}