package com.aiplms.auth.config;

import com.aiplms.auth.outbox.OutboxProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        AuthProperties.class,
        OutboxProperties.class
})
public class AppConfig {
}
