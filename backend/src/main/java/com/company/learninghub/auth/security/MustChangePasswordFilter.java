package com.company.learninghub.auth.security;

import com.company.learninghub.common.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

@Component
public class MustChangePasswordFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    public MustChangePasswordFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser
                && authenticatedUser.isMustChangePassword()
                && !isAllowedWhilePasswordChangeRequired(request)) {
            writeForbidden(response, request.getRequestURI());
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAllowedWhilePasswordChangeRequired(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (HttpMethod.GET.matches(request.getMethod()) && "/api/v1/health".equals(path)) {
            return true;
        }
        if (path.startsWith("/actuator/health")) {
            return true;
        }
        if (HttpMethod.GET.matches(request.getMethod()) && "/api/v1/auth/me".equals(path)) {
            return true;
        }
        return HttpMethod.POST.matches(request.getMethod()) && "/api/v1/auth/change-password".equals(path);
    }

    private void writeForbidden(HttpServletResponse response, String path) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                "Password change required before accessing this resource",
                path,
                null
        );
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
