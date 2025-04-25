package com.FinalProject.TodoApp.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Bean cho PasswordEncoder (BCrypt)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Cấu hình bảo mật các endpoint
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())  // Tắt CSRF vì đây là API, không cần thiết
                .authorizeHttpRequests(authz -> authz
                        // Cho phép truy cập không cần xác thực với các API đăng ký và xác minh OTP
                        .requestMatchers("/api/auth/register",
                                "/api/auth/verify-code",
                                "/api/user/login",
                                "/api/auth/forgot-password",
                                "/api/auth/forgot-password/verify-code",
                                "/api/auth/reset-password").permitAll()
                        .anyRequest().authenticated()  // Các yêu cầu API khác đều yêu cầu xác thực
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized"))  // Trả về lỗi 401 khi không xác thực
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // Không sử dụng session cho API
                );

        return http.build();
    }
}
