package com.tss.LoanEmiScheduler.filter;

import com.tss.LoanEmiScheduler.enums.LogTag;
import com.tss.LoanEmiScheduler.service.CustomUserDetailsService;
import com.tss.LoanEmiScheduler.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
@Slf4j
public class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    @Lazy
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        log.info("{} Filter: Initialized", LogTag.SECURITY.getValue());

        String authHeader = request.getHeader("Authorization");
        String token = null;
        String identifier = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            identifier = jwtService.extractIdentifier(token);
            log.info("{} Filter: For user {}", LogTag.SECURITY.getValue(), identifier);
        }

        if (identifier != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(identifier);
            if (jwtService.validateToken(token, userDetails)) {
                List<String> roles = jwtService.extractRoles(token);
                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                authorities
                        );
                authenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                log.info("{} Filter: Authentication token configured for user {} with roles {}", LogTag.SECURITY.getValue(), identifier, roles);
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }else {
                log.warn("{} Filter: Token invalid for user {}", LogTag.SECURITY.getValue(), identifier);
            }
        }
        filterChain.doFilter(request, response);
    }
}
