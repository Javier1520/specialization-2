package com.epam.gym.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class TransactionIdInterceptor implements HandlerInterceptor {
    private static final String TRANSACTION_ID_HEADER = "X-Transaction-Id";
    private static final String TRANSACTION_ID_MDC_KEY = "transactionId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String transactionId = request.getHeader(TRANSACTION_ID_HEADER);
        if (transactionId == null || transactionId.isBlank()) {
            transactionId = TransactionIdGenerator.generate();
        }

        MDC.put(TRANSACTION_ID_MDC_KEY, transactionId);
        response.setHeader(TRANSACTION_ID_HEADER, transactionId);

        log.info("Request: {} {} - TransactionId: {}", request.getMethod(), request.getRequestURI(), transactionId);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        MDC.remove(TRANSACTION_ID_MDC_KEY);
    }
}


