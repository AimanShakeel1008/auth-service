// src/main/java/com/aiplms/auth/config/AppConfig.java
package com.aiplms.auth.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AuthProperties.class)
public class AppConfig {
}
