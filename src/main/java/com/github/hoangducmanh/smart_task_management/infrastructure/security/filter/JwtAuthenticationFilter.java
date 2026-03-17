package com.github.hoangducmanh.smart_task_management.infrastructure.security.filter;

import java.io.IOException;
import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.github.hoangducmanh.smart_task_management.infrastructure.security.AuthenticateUser;
import com.github.hoangducmanh.smart_task_management.infrastructure.security.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, 
                                    @NonNull HttpServletResponse response, 
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null|| !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        final String jwt = authHeader.substring(7);

        if(!jwtService.isTokenValid(jwt)) {
            filterChain.doFilter(request, response);
            return;
        }
        if(SecurityContextHolder.getContext().getAuthentication() == null) {

                final AuthenticateUser authenticateUser = new AuthenticateUser(
                    UUID.fromString(jwtService.extractUserId(jwt)),
                    jwtService.extractRoles(jwt) 
            );
                var authToken = new UsernamePasswordAuthenticationToken(
                    authenticateUser,
                    null,
                    authenticateUser.roles().stream().map(SimpleGrantedAuthority::new).toList()
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
            
        }
        filterChain.doFilter(request, response);
    }

}
