package com.epam.gym.security;

import com.epam.gym.service.AuthenticationService;
import com.epam.gym.util.LogUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationInterceptor implements HandlerInterceptor {

    private final AuthenticationService authenticationService;
    private final LogUtils logUtils;

    private static final String USERNAME_HEADER = "username";
    private static final String PASSWORD_HEADER = "password";
    private static final String AUTHENTICATED_USERNAME_ATTRIBUTE = "authenticatedUsername";

    private static final Set<PublicEndpoint> PUBLIC_ENDPOINTS = Set.of(
        new PublicEndpoint("/api/v1/trainees/register", HttpMethod.POST),
        new PublicEndpoint("/api/v1/trainers/register", HttpMethod.POST),
        new PublicEndpoint("/api/v1/auth/login", HttpMethod.GET)
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {

        String path = request.getRequestURI();
        HttpMethod method = HttpMethod.valueOf(request.getMethod());

        if (shouldAllowPublicAccess(path, method)) {
            return true;
        }

        String username = request.getHeader(USERNAME_HEADER);
        String password = request.getHeader(PASSWORD_HEADER);

        if (!credentialsPresent(username, password, response)) {
            return false;
        }

        return handleAuthentication(username, password, request, response);
    }

    private boolean shouldAllowPublicAccess(String path, HttpMethod method) {
        boolean isPublic = isPublicEndpoint(path, method);
        if (isPublic) {
            logUtils.debug(log, "Allowing access to public endpoint: {} {}", method, path);
        }
        return isPublic;
    }

    private boolean credentialsPresent(String username, String password, HttpServletResponse response
    ) throws Exception {

        boolean missing = username == null || username.isBlank()
                       || password == null || password.isBlank();

        if (missing) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Authentication required. Please provide username and password headers.");
            return false;
        }
        return true;
    }

    private boolean handleAuthentication(String username, String password, HttpServletRequest request,
            HttpServletResponse response
    ) throws Exception {

        try {
            authenticationService.authenticate(username, password);
            attachAuthenticatedUser(username, request);
            logUtils.debug(log, "Authentication successful for user: {}", username);
            return true;

        } catch (Exception e) {
            logUtils.warn(log, "Authentication failed for username: {}", username, e);
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
            return false;
        }
    }

    private void attachAuthenticatedUser(String username, HttpServletRequest request) {
        request.setAttribute(AUTHENTICATED_USERNAME_ATTRIBUTE, username);
    }

    private boolean isPublicEndpoint(String path, HttpMethod method) {
        return PUBLIC_ENDPOINTS.stream()
                .anyMatch(endpoint -> endpoint.matches(path, method));
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message
    ) throws Exception {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }

    private static class PublicEndpoint {
        private final String path;
        private final HttpMethod method;

        PublicEndpoint(String path, HttpMethod method) {
            this.path = path;
            this.method = method;
        }

        boolean matches(String requestPath, HttpMethod requestMethod) {
            return requestPath.startsWith(this.path) && this.method == requestMethod;
        }
    }
}
