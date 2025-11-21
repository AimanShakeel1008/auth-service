package com.aiplms.auth.security;

import com.aiplms.auth.service.RedisTokenBucketService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RedisTokenBucketService bucketService;

    private static final Map<String, Rule> rules = new HashMap<>();
    static {
        rules.put("POST:/api/v1/auth/login", new Rule(5, Duration.ofMinutes(1)));
        rules.put("POST:/api/v1/auth/password-reset/request", new Rule(3, Duration.ofMinutes(15)));
        rules.put("POST:/api/v1/auth/register", new Rule(10, Duration.ofMinutes(10)));
        rules.put("DEFAULT_AUTH", new Rule(100, Duration.ofMinutes(1)));
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/v1/auth");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String key = buildKey(request);
        Rule rule = rules.getOrDefault(request.getMethod() + ":" + request.getRequestURI(), rules.get("DEFAULT_AUTH"));

        long remaining = bucketService.tryConsume(key, rule.limit, rule.window);
        if (remaining >= 0) {
            response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            String body = String.format("{\"timestamp\":\"%s\",\"status\":429,\"errorCode\":\"AUTH_ERR_RATE_LIMIT\",\"message\":\"Too many requests\",\"path\":\"%s\"}",
                    java.time.OffsetDateTime.now().toString(), request.getRequestURI());
            response.getWriter().write(body);
        }
    }

    private String buildKey(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        } else {
            ip = ip.split(",")[0].trim();
        }
        return ip + ":" + request.getMethod() + ":" + request.getRequestURI();
    }

    static class Rule {
        final long limit;
        final Duration window;
        Rule(long limit, Duration window) {
            this.limit = limit;
            this.window = window;
        }
    }
}
