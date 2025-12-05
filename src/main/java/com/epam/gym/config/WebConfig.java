package com.epam.gym.config;

import com.epam.gym.util.TransactionIdInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
  private final TransactionIdInterceptor transactionIdInterceptor;

  public WebConfig(TransactionIdInterceptor transactionIdInterceptor) {
    this.transactionIdInterceptor = transactionIdInterceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(transactionIdInterceptor);
  }
}
