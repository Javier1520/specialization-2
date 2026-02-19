package com.epam.gym.config;

import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FeignClientInterceptorTest {

    private FeignClientInterceptor interceptor;
    private RequestTemplate template;

    @BeforeEach
    void setUp() {
        interceptor = new FeignClientInterceptor();
        template = new RequestTemplate();
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void apply_shouldAddAuthorizationHeader_whenTokenInSecurityContext() {
        String token = "test.jwt.token";

        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.addHeader("Authorization", "Bearer " + token);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));

        interceptor.apply(template);

        assertEquals("Bearer " + token, template.headers().get("Authorization").iterator().next());
    }

    @Test
    void apply_shouldAddAuthorizationHeader_whenTokenInRequestAttributes() {
        String token = "test.jwt.token";
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        interceptor.apply(template);

        assertEquals("Bearer " + token, template.headers().get("Authorization").iterator().next());
    }

    @Test
    void apply_shouldNotAddHeader_whenNoTokenFound() {
        interceptor.apply(template);

        assertNull(template.headers().get("Authorization"));
    }
}
