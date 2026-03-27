package com.bank.security;

import com.bank.service.impl.AccessTokenBlacklistService;
import com.bank.util.JwtAuthenticationFilter;
import com.bank.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private AccessTokenBlacklistService accessTokenBlacklistService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;
    @Mock
    private UserDetails userDetails;
    @Mock
    private java.io.PrintWriter printWriter;
    @InjectMocks
    private JwtAuthenticationFilter filter;

    @Test
    void doFilterInternal_noAuthHeader() throws Exception {
        try (AutoCloseable mocks = MockitoAnnotations.openMocks(this)) {
            SecurityContextHolder.clearContext();
            when(response.getWriter()).thenReturn(printWriter);
            when(userDetails.getAuthorities()).thenReturn(java.util.Collections.emptyList());
            when(request.getServletPath()).thenReturn("/api/protected");
            when(request.getHeader(anyString())).thenReturn(null);
            filter.doFilterInternal(request, response, filterChain);
            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            verify(response).getWriter();
            verify(printWriter).write(contains("Missing or invalid Authorization header."));
            verifyNoMoreInteractions(filterChain);
        }
    }

    @Test
    void doFilterInternal_blacklistedToken() throws Exception {
        try (AutoCloseable mocks = MockitoAnnotations.openMocks(this)) {
            SecurityContextHolder.clearContext();
            when(response.getWriter()).thenReturn(printWriter);
            when(userDetails.getAuthorities()).thenReturn(java.util.Collections.emptyList());
            when(request.getServletPath()).thenReturn("/api/protected");
            when(request.getHeader(anyString())).thenReturn("Bearer blacklistedtoken");
            when(accessTokenBlacklistService.isTokenBlacklisted("blacklistedtoken")).thenReturn(true);
            filter.doFilterInternal(request, response, filterChain);
            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            verify(response).getWriter();
            verify(printWriter).write(contains("blacklisted"));
            verifyNoMoreInteractions(filterChain);
        }
    }

    @Test
    void doFilterInternal_validToken_setsAuthentication() throws Exception {
        try (AutoCloseable mocks = MockitoAnnotations.openMocks(this)) {
            SecurityContextHolder.clearContext();
            when(response.getWriter()).thenReturn(printWriter);
            when(userDetails.getAuthorities()).thenReturn(java.util.Collections.emptyList());
            when(request.getServletPath()).thenReturn("/api/protected");
            when(request.getHeader(anyString())).thenReturn("Bearer validtoken");
            when(accessTokenBlacklistService.isTokenBlacklisted("validtoken")).thenReturn(false);
            when(jwtUtil.extractUsername("validtoken")).thenReturn("user");
            when(jwtUtil.isTokenValid("validtoken")).thenReturn(true);
            when(userDetailsService.loadUserByUsername("user")).thenReturn(userDetails);
            when(request.getRemoteAddr()).thenReturn("127.0.0.1");
            when(request.getSession(false)).thenReturn(null);
            filter.doFilterInternal(request, response, filterChain);
            assertNotNull(SecurityContextHolder.getContext().getAuthentication());
            verify(filterChain).doFilter(request, response);
        }
    }

    @Test
    void doFilterInternal_jwtException_doesNotAuthenticate() throws Exception {
        try (AutoCloseable mocks = MockitoAnnotations.openMocks(this)) {
            SecurityContextHolder.clearContext();
            when(response.getWriter()).thenReturn(printWriter);
            when(userDetails.getAuthorities()).thenReturn(java.util.Collections.emptyList());
            when(request.getServletPath()).thenReturn("/api/protected");
            when(request.getHeader(anyString())).thenReturn("Bearer invalidtoken");
            when(accessTokenBlacklistService.isTokenBlacklisted("invalidtoken")).thenReturn(false);
            when(jwtUtil.extractUsername("invalidtoken")).thenThrow(new RuntimeException("JWT error"));
            filter.doFilterInternal(request, response, filterChain);
            assertNull(SecurityContextHolder.getContext().getAuthentication());
            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            verify(response).getWriter();
            verify(printWriter).write(contains("Invalid or expired JWT token."));
            verifyNoMoreInteractions(filterChain);
        }
    }

    @Test
    void doFilterInternal_authHeaderNotBearer() throws Exception {
        try (AutoCloseable mocks = MockitoAnnotations.openMocks(this)) {
            SecurityContextHolder.clearContext();
            when(response.getWriter()).thenReturn(printWriter);
            when(userDetails.getAuthorities()).thenReturn(java.util.Collections.emptyList());
            when(request.getServletPath()).thenReturn("/api/protected");
            when(request.getHeader(anyString())).thenReturn("Basic sometoken");
            filter.doFilterInternal(request, response, filterChain);
            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            verify(response).getWriter();
            verify(printWriter).write(contains("Missing or invalid Authorization header."));
            verifyNoMoreInteractions(filterChain);
        }
    }

    @Test
    void doFilterInternal_emptyBearerToken() throws Exception {
        try (AutoCloseable mocks = MockitoAnnotations.openMocks(this)) {
            SecurityContextHolder.clearContext();
            when(response.getWriter()).thenReturn(printWriter);
            when(userDetails.getAuthorities()).thenReturn(java.util.Collections.emptyList());
            when(request.getServletPath()).thenReturn("/api/protected");
            when(request.getHeader(anyString())).thenReturn("Bearer ");
            when(accessTokenBlacklistService.isTokenBlacklisted("")).thenReturn(false);
            when(jwtUtil.extractUsername("")).thenReturn(null);
            filter.doFilterInternal(request, response, filterChain);
            assertNull(SecurityContextHolder.getContext().getAuthentication());
            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            verify(response).getWriter();
            verify(printWriter).write(contains("Invalid or expired JWT token."));
            verifyNoMoreInteractions(filterChain);
        }
    }

    @Test
    void doFilterInternal_tokenWithNullUsername() throws Exception {
        try (AutoCloseable mocks = MockitoAnnotations.openMocks(this)) {
            SecurityContextHolder.clearContext();
            when(response.getWriter()).thenReturn(printWriter);
            when(userDetails.getAuthorities()).thenReturn(java.util.Collections.emptyList());
            when(request.getServletPath()).thenReturn("/api/protected");
            when(request.getHeader(anyString())).thenReturn("Bearer sometoken");
            when(accessTokenBlacklistService.isTokenBlacklisted("sometoken")).thenReturn(false);
            when(jwtUtil.extractUsername("sometoken")).thenReturn(null);
            filter.doFilterInternal(request, response, filterChain);
            assertNull(SecurityContextHolder.getContext().getAuthentication());
            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            verify(response).getWriter();
            verify(printWriter).write(contains("Invalid or expired JWT token."));
            verifyNoMoreInteractions(filterChain);
        }
    }

    @Test
    void doFilterInternal_tokenWithExistingAuthentication() throws Exception {
        try (AutoCloseable mocks = MockitoAnnotations.openMocks(this)) {
            SecurityContextHolder.clearContext();
            when(response.getWriter()).thenReturn(printWriter);
            when(userDetails.getAuthorities()).thenReturn(java.util.Collections.emptyList());
            when(request.getServletPath()).thenReturn("/api/protected");
            when(request.getHeader(anyString())).thenReturn("Bearer sometoken");
            when(accessTokenBlacklistService.isTokenBlacklisted("sometoken")).thenReturn(false);
            when(jwtUtil.extractUsername("sometoken")).thenReturn("user");
            org.springframework.security.core.Authentication existingAuth = mock(org.springframework.security.core.Authentication.class);
            SecurityContextHolder.getContext().setAuthentication(existingAuth);
            filter.doFilterInternal(request, response, filterChain);
            // Should not overwrite existing authentication
            verify(filterChain).doFilter(request, response);
            assertSame(existingAuth, SecurityContextHolder.getContext().getAuthentication());
        }
    }

    @Test
    void doFilterInternal_tokenWithInvalidToken() throws Exception {
        try (AutoCloseable mocks = MockitoAnnotations.openMocks(this)) {
            SecurityContextHolder.clearContext();
            when(response.getWriter()).thenReturn(printWriter);
            when(userDetails.getAuthorities()).thenReturn(java.util.Collections.emptyList());
            when(request.getServletPath()).thenReturn("/api/protected");
            when(request.getHeader(anyString())).thenReturn("Bearer sometoken");
            when(accessTokenBlacklistService.isTokenBlacklisted("sometoken")).thenReturn(false);
            when(jwtUtil.extractUsername("sometoken")).thenReturn("user");
            when(jwtUtil.isTokenValid("sometoken")).thenReturn(false);
            filter.doFilterInternal(request, response, filterChain);
            assertNull(SecurityContextHolder.getContext().getAuthentication());
            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            verify(response).getWriter();
            verify(printWriter).write(contains("Invalid or expired JWT token."));
            verifyNoMoreInteractions(filterChain);
        }
    }

    @Test
    void doFilterInternal_userDetailsServiceThrowsException() throws Exception {
        try (AutoCloseable mocks = MockitoAnnotations.openMocks(this)) {
            SecurityContextHolder.clearContext();
            when(response.getWriter()).thenReturn(printWriter);
            when(userDetails.getAuthorities()).thenReturn(java.util.Collections.emptyList());
            when(request.getServletPath()).thenReturn("/api/protected");
            when(request.getHeader(anyString())).thenReturn("Bearer sometoken");
            when(accessTokenBlacklistService.isTokenBlacklisted("sometoken")).thenReturn(false);
            when(jwtUtil.extractUsername("sometoken")).thenReturn("user");
            when(jwtUtil.isTokenValid("sometoken")).thenReturn(true);
            when(userDetailsService.loadUserByUsername("user")).thenThrow(new RuntimeException("User not found"));
            filter.doFilterInternal(request, response, filterChain);
            assertNull(SecurityContextHolder.getContext().getAuthentication());
            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            verify(response).getWriter();
            verify(printWriter).write(contains("Invalid or expired JWT token."));
            verifyNoMoreInteractions(filterChain);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "/swagger-ui/index.html",
        "/v3/api-docs",
        "/swagger-resources",
        "/api/auth/login",
        "/api/auth/generate-otp",
        "/api/auth/verify-otp",
        "/api/auth/check-customer"
    })
    void doFilterInternal_bypassPaths_allRequired(String path) throws Exception {
        try (AutoCloseable mocks = MockitoAnnotations.openMocks(this)) {
            SecurityContextHolder.clearContext();
            when(response.getWriter()).thenReturn(printWriter);
            when(userDetails.getAuthorities()).thenReturn(java.util.Collections.emptyList());
            when(request.getServletPath()).thenReturn(path);
            filter.doFilterInternal(request, response, filterChain);
            verify(filterChain).doFilter(request, response);
            verifyNoInteractions(jwtUtil);
            verifyNoInteractions(userDetailsService);
            verifyNoInteractions(accessTokenBlacklistService);
        }
    }
}
