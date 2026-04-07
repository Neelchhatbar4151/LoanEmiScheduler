package com.tss.LoanEmiScheduler.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
public class LogInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 1. Generate a unique Trace ID for this specific request
        String traceId = UUID.randomUUID().toString().substring(0, 8);

        // 2. Get the User from SecurityContext (if logged in)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String user = (auth != null && auth.isAuthenticated()) ? auth.getName() : "ANONYMOUS";

        // 3. Fill the MDC "Notepad" based on your XML pattern keys
        MDC.put("traceId", traceId);
        MDC.put("method", request.getMethod());
        MDC.put("uri", request.getRequestURI());
        MDC.put("ip", request.getRemoteAddr());
        MDC.put("user", user);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 4. CRITICAL: Clear the MDC so the next request doesn't see old data
        MDC.clear();
    }
}