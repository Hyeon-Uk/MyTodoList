package com.hyeonuk.todo.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyeonuk.todo.integ.dto.ErrorMessageDTO;
import com.hyeonuk.todo.integ.util.JwtProvider;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private final ObjectMapper mapper;

    public String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer")) {
            return null;
        } else {
            return authorization.substring(7);
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String accessToken = resolveToken(request);
        if (accessToken != null && jwtProvider.isValidToken(accessToken)) {

            Authentication auth = jwtProvider.getAuthentication(accessToken);
            SecurityContextHolder.getContext().setAuthentication(auth);

            filterChain.doFilter(request, response);
            return;
        }
        filterChain.doFilter(request,response);
    }
}
