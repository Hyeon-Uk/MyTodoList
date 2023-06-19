package com.hyeonuk.todo.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyeonuk.todo.integ.dto.ErrorMessageDTO;
import com.hyeonuk.todo.integ.util.JwtProvider;
import com.hyeonuk.todo.security.filter.JwtAuthenticationFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.io.IOException;
import java.util.List;

@Configuration
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    private final String[] whiteList = {"/auth/login", "/auth/regist"};
    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> {
                    csrf.disable();//csrf 비활성화
                })
                .sessionManagement(session -> {
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);//세션 비활성화
                })
                .httpBasic(httpBasic -> {
                    httpBasic.disable();//HTTP Basic Authentication 비활성화
                })
                .cors(cors -> {//CORS 설정
                    CorsConfigurationSource source = request -> {
                        CorsConfiguration corsConfig = new CorsConfiguration();
                        corsConfig.setAllowedOrigins(List.of("*"));
                        corsConfig.setAllowedMethods(List.of("*"));
                        return corsConfig;
                    };
                    cors.configurationSource(source);
                });

        http.authorizeHttpRequests(req ->
                req.requestMatchers(whiteList).permitAll()
                        .anyRequest().authenticated());

        http.userDetailsService(userDetailsService);

        //인증이 필요한 요청 이전에 jwt authentication필터를 적용
        http.addFilterBefore(new JwtAuthenticationFilter(jwtProvider, objectMapper), UsernamePasswordAuthenticationFilter.class);

        //인증 거부 or 실패 시 핸들러 적용
        http.exceptionHandling(exception -> {
            exception
                    .accessDeniedHandler(new AccessDeniedHandler() {//권한이 없어서 접근x일경우
                        @Override
                        public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
                            ErrorMessageDTO error = ErrorMessageDTO.builder()
                                    .status(HttpServletResponse.SC_FORBIDDEN)
                                    .message("권한이 없는 사용자입니다.")
                                    .build();

                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.getWriter().write(objectMapper.writeValueAsString(error));
                        }
                    })
                    .authenticationEntryPoint(new AuthenticationEntryPoint() {//인증 문제가 발생한 경우
                        @Override
                        public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
                            ErrorMessageDTO error = ErrorMessageDTO.builder()
                                    .status(HttpServletResponse.SC_UNAUTHORIZED)
                                    .message("인증되지 않은 사용자입니다.")
                                    .build();

                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write(objectMapper.writeValueAsString(error));
                        }
                    });
        });

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
