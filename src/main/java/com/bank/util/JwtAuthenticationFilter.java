package com.bank.util;
import com.bank.service.impl.AccessTokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

	private final JwtUtil jwtUtil;
	private final UserDetailsService userDetailsService;
	private final AccessTokenBlacklistService accessTokenBlacklistService;

    private void writeErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.getWriter().write(message);
        response.flushBuffer();
    }

    private boolean validateAndAuthenticateToken(String token, HttpServletRequest request) {
        try {
            String username = jwtUtil.extractUsername(token);
            if (username != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null &&
                    jwtUtil.isTokenValid(token)) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
                return true;
            }
        } catch (Exception e) {
            log.debug("JWT parsing/validation failed: {}", e.getMessage());
        }
        return false;
    }

    @Override
    public void doFilterInternal(@NonNull HttpServletRequest request,
                                 @NonNull HttpServletResponse response,
                                 @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getServletPath();
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        log.info("JwtAuthenticationFilter: path={}, Authorization={}", path, header);
        // Skip JWT filter for Swagger and public auth endpoints
        if (path.startsWith("/swagger-ui") ||
            path.startsWith("/v3/api-docs") ||
            path.startsWith("/swagger-resources") ||
            path.equals("/api/auth/login") ||
            path.equals("/api/auth/generate-otp") ||
            path.equals("/api/auth/verify-otp") ||
            path.equals("/api/auth/check-customer")) {
            log.info("JwtAuthenticationFilter: Skipping JWT validation for path={}", path);
            filterChain.doFilter(request, response);
            return;
        }
        if (header == null || !header.startsWith("Bearer ")) {
            log.warn("JwtAuthenticationFilter: Missing or invalid Authorization header for path={}", path);
            writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header.");
            return;
        } else {
            String token = header.substring(7);
            // Blacklist check
            if (accessTokenBlacklistService.isTokenBlacklisted(token)) {
                log.warn("JwtAuthenticationFilter: Token is blacklisted for path={}", path);
                writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token is blacklisted. Please login again.");
                return;
            }
            // If authentication already exists, just continue the filter chain
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                log.info("JwtAuthenticationFilter: Authentication already exists for path={}, skipping JWT validation", path);
                filterChain.doFilter(request, response);
                return;
            }
            if (!validateAndAuthenticateToken(token, request)) {
                log.warn("JwtAuthenticationFilter: Invalid or expired JWT token for path={}", path);
                writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired JWT token.");
                return;
            }
        }
        log.info("JwtAuthenticationFilter: Passing request down filter chain for path={}", path);
        filterChain.doFilter(request, response);
    }
}
