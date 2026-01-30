package com.epam.gym.workload.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class TransactionFilter implements Filter {

    private static final String TRANSACTION_ID_HEADER = "X-Transaction-Id";
    private static final String MDC_KEY = "transactionId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest && response instanceof HttpServletResponse httpResponse) {
            String transactionId = httpRequest.getHeader(TRANSACTION_ID_HEADER);
            if (transactionId == null || transactionId.isEmpty()) {
                transactionId = UUID.randomUUID().toString();
            }

            MDC.put(MDC_KEY, "[" + transactionId + "]");
            httpResponse.setHeader(TRANSACTION_ID_HEADER, transactionId);

            log.info("Request: {} {}", httpRequest.getMethod(), httpRequest.getRequestURI());

            try {
                chain.doFilter(request, response);
                log.info("Response: Status {}", httpResponse.getStatus());
            } finally {
                MDC.remove(MDC_KEY);
            }
        } else {
            chain.doFilter(request, response);
        }
    }
}
