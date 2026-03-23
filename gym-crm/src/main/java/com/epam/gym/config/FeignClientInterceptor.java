package com.epam.gym.config;

import com.epam.gym.util.LogUtils;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class FeignClientInterceptor implements RequestInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final Logger log = LoggerFactory.getLogger(FeignClientInterceptor.class);

    private final LogUtils logUtils;

    public FeignClientInterceptor(LogUtils logUtils) {
        this.logUtils = logUtils;
    }

    @Override
    public void apply(RequestTemplate template) {
        String token = getJwtToken();
        if (token != null) {
            template.header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token);
            logUtils.debug(log, "Added Authorization header to Feign request");
        } else {
            logUtils.warn(log, "No JWT token found in SecurityContext or RequestContext to propagate");
        }

        String transactionId = MDC.get("transactionId");
        if (transactionId != null) {
            template.header("X-Transaction-Id", transactionId);
            logUtils.debug(log, "Propagated transactionId to Feign request: {}", transactionId);
        }
    }

    private String getJwtToken() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String authHeader = request.getHeader(AUTHORIZATION_HEADER);
            if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
                return authHeader.substring(BEARER_PREFIX.length());
            }
        }
        return null;
    }
}
