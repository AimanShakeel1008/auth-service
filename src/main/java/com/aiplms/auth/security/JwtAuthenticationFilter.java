package com.aiplms.auth.security;

import com.aiplms.auth.entity.User;
import com.aiplms.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String header = request.getHeader("Authorization");
            if (!StringUtils.hasText(header) || !header.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = header.substring(7);
            JwtService.JwtClaims claims = jwtService.parseAndValidate(token);

            // Expecting "id" claim (UUID) as created in AuthServiceImpl
            String idStr = claims.getClaimAsString("id");
            if (idStr == null) {
                log.debug("JWT has no 'id' claim - skipping authentication");
                filterChain.doFilter(request, response);
                return;
            }

            UUID userId = UUID.fromString(idStr);
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                log.debug("User not found for id from JWT: {}", userId);
                filterChain.doFilter(request, response);
                return;
            }

            List<SimpleGrantedAuthority> authorities = user.getRoles() == null
                    ? List.of()
                    : user.getRoles().stream()
                    .map(r -> new SimpleGrantedAuthority(r.getName()))
                    .collect(Collectors.toList());

            // Build Authentication; leave credentials null
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(user, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (JwtService.JwtValidationException ex) {
            // Let authentication entry point handle writing the response.
            log.debug("JWT validation failed: {}", ex.getMessage());
            // Clear context just in case
            SecurityContextHolder.clearContext();
            // Re-throw as ServletException to trigger AuthenticationEntryPoint via filter chain
            throw new ServletException(ex);
        } catch (Exception e) {
            log.error("Unexpected error while processing JWT authentication", e);
            SecurityContextHolder.clearContext();
            // continue the filter chain as unauthenticated
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // Let security rules + SecurityConfig decide which endpoints require authentication.
        // Filter will execute for requests that possibly contain Authorization header.
        return false;
    }
}

