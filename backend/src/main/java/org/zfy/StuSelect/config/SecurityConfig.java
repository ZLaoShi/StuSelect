package org.zfy.StuSelect.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.zfy.StuSelect.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/courses").permitAll()
                .requestMatchers("/api/courses/**").hasRole("ADMIN")
                .requestMatchers("/api/students").hasRole("ADMIN")
                // 添加学生访问自己信息的权限
                .requestMatchers("/api/students/user/**").hasAnyRole("ADMIN", "STUDENT")
                // 管理员才能访问所有学生信息
                .requestMatchers("/api/students/**").hasRole("ADMIN")
                .requestMatchers("/api/users/{id}").authenticated()  // 允许已认证用户访问
                .requestMatchers("/api/users/**").hasRole("ADMIN")
                .requestMatchers("/api/dashboard/admin").hasRole("ADMIN")
                .requestMatchers("/api/dashboard/student").hasAnyRole("ADMIN", "STUDENT")
                .requestMatchers("/api/selections/**").authenticated()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}