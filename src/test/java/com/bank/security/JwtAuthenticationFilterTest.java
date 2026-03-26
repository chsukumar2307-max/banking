package com.bank.security;
import com.bank.service.impl.AccessTokenBlacklistService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
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
    @Mock
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext();
        try {
            when(response.getWriter()).thenReturn(printWriter);
        } catch (Exception ignored) {
            // This is safe in tests: PrintWriter is only needed for certain test cases
        }
        // Ensure userDetails.getAuthorities() is stubbed for all tests
        when(userDetails.getAuthorities()).thenReturn(java.util.Collections.emptyList());
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    void doFilterInternal_swaggerBypass() throws ServletException, IOException {
        when(request.getServletPath()).thenReturn("/swagger-ui/index.html");
        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtUtil, userDetailsService, accessTokenBlacklistService);
    }

    @Test
    void doFilterInternal_noAuthHeader() throws ServletException, IOException {
        when(request.getServletPath()).thenReturn("/api/protected");
        when(request.getHeader(anyString())).thenReturn(null);
        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_blacklistedToken() throws Exception {
        when(request.getServletPath()).thenReturn("/api/protected");
        when(request.getHeader(anyString())).thenReturn("Bearer blacklistedtoken");
        when(accessTokenBlacklistService.isTokenBlacklisted("blacklistedtoken")).thenReturn(true);
        filter.doFilterInternal(request, response, filterChain);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).getWriter();
        verify(printWriter).write(contains("blacklisted"));
        verifyNoMoreInteractions(filterChain);
    }

    @Test
    void doFilterInternal_validToken_setsAuthentication() throws Exception {
        when(request.getServletPath()).thenReturn("/api/protected");
        when(request.getHeader(anyString())).thenReturn("Bearer validtoken");
        when(accessTokenBlacklistService.isTokenBlacklisted("validtoken")).thenReturn(false);
        when(jwtUtil.extractUsername("validtoken")).thenReturn("user");
        when(jwtUtil.isTokenValid("validtoken")).thenReturn(true);
        when(userDetailsService.loadUserByUsername("user")).thenReturn(userDetails);
        // userDetails.getAuthorities() is already stubbed in setUp
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getSession(false)).thenReturn(null);
        filter.doFilterInternal(request, response, filterChain);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_jwtException_doesNotAuthenticate() throws Exception {
        when(request.getServletPath()).thenReturn("/api/protected");
        when(request.getHeader(anyString())).thenReturn("Bearer invalidtoken");
        when(accessTokenBlacklistService.isTokenBlacklisted("invalidtoken")).thenReturn(false);
        when(jwtUtil.extractUsername("invalidtoken")).thenThrow(new RuntimeException("JWT error"));
        filter.doFilterInternal(request, response, filterChain);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_authHeaderNotBearer() throws Exception {
        when(request.getServletPath()).thenReturn("/api/protected");
        when(request.getHeader(anyString())).thenReturn("Basic sometoken");
        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_emptyBearerToken() throws Exception {
        when(request.getServletPath()).thenReturn("/api/protected");
        when(request.getHeader(anyString())).thenReturn("Bearer ");
        when(accessTokenBlacklistService.isTokenBlacklisted("")).thenReturn(false);
        when(jwtUtil.extractUsername("")).thenReturn(null);
        filter.doFilterInternal(request, response, filterChain);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_tokenWithNullUsername() throws Exception {
        when(request.getServletPath()).thenReturn("/api/protected");
        when(request.getHeader(anyString())).thenReturn("Bearer sometoken");
        when(accessTokenBlacklistService.isTokenBlacklisted("sometoken")).thenReturn(false);
        when(jwtUtil.extractUsername("sometoken")).thenReturn(null);
        filter.doFilterInternal(request, response, filterChain);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_tokenWithExistingAuthentication() throws Exception {
        when(request.getServletPath()).thenReturn("/api/protected");
        when(request.getHeader(anyString())).thenReturn("Bearer sometoken");
        when(accessTokenBlacklistService.isTokenBlacklisted("sometoken")).thenReturn(false);
        when(jwtUtil.extractUsername("sometoken")).thenReturn("user");
        SecurityContextHolder.getContext().setAuthentication(mock(org.springframework.security.core.Authentication.class));
        filter.doFilterInternal(request, response, filterChain);
        // Should not overwrite existing authentication
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_tokenWithInvalidToken() throws Exception {
        when(request.getServletPath()).thenReturn("/api/protected");
        when(request.getHeader(anyString())).thenReturn("Bearer sometoken");
        when(accessTokenBlacklistService.isTokenBlacklisted("sometoken")).thenReturn(false);
        when(jwtUtil.extractUsername("sometoken")).thenReturn("user");
        when(jwtUtil.isTokenValid("sometoken")).thenReturn(false);
        filter.doFilterInternal(request, response, filterChain);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_userDetailsServiceThrowsException() throws Exception {
        when(request.getServletPath()).thenReturn("/api/protected");
        when(request.getHeader(anyString())).thenReturn("Bearer sometoken");
        when(accessTokenBlacklistService.isTokenBlacklisted("sometoken")).thenReturn(false);
        when(jwtUtil.extractUsername("sometoken")).thenReturn("user");
        when(jwtUtil.isTokenValid("sometoken")).thenReturn(true);
        when(userDetailsService.loadUserByUsername("user")).thenThrow(new RuntimeException("User not found"));
        filter.doFilterInternal(request, response, filterChain);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}
