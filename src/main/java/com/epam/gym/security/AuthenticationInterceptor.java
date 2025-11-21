package com.epam.gym.security;

import com.epam.gym.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationInterceptor implements HandlerInterceptor {
    private final AuthenticationService authenticationService;

    private static final String USERNAME_HEADER = "username";
    private static final String PASSWORD_HEADER = "password";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();

        // Allow these endpoints without authentication
        if (path.startsWith("/api/v1/trainees/register") && "POST".equals(request.getMethod())) {
            return true;
        }
        if (path.startsWith("/api/v1/trainers/register") && "POST".equals(request.getMethod())) {
            return true;
        }
        if (path.startsWith("/api/v1/auth/login") && "GET".equals(request.getMethod())) {
            return true;
        }

        // Extract credentials from headers
        String username = request.getHeader(USERNAME_HEADER);
        String password = request.getHeader(PASSWORD_HEADER);

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Authentication required. Please provide username and password headers.\"}");
            return false;
        }

        try {
            authenticationService.authenticate(username, password);
            // Store username in request attribute for use in controllers
            request.setAttribute("authenticatedUsername", username);
            return true;
        } catch (Exception e) {
            log.warn("Authentication failed for username: {}", username, e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
            return false;
        }
    }
}


