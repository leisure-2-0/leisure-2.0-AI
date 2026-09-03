package com.leisure.ai.global.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// 메인 백엔드만 /ai/index/**, /ai/chat을 호출할 수 있도록 내부 API 키 헤더를 검증.
@Component
public class InternalAuthFilter extends OncePerRequestFilter {
    // 메인 벡엔드에서 요청 시 헤더에 넣어 보내는 키 이름  
    private static final String HEADER_NAME = "X-Internal-Api-Key"; 
    private final String expectedApiKey;

    public InternalAuthFilter(@Value("${internal-auth.api-key:}") String expectedApiKey) { this.expectedApiKey = expectedApiKey;}

    @Override // 키 검증 
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (isProtectedPath(request.getRequestURI()) && !isAuthorized(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean isProtectedPath(String path) {
        return path.startsWith("/ai/index") || path.startsWith("/ai/chat");
    }

    private boolean isAuthorized(HttpServletRequest request) {
        // 키가 아예 설정 안 돼 있으면 fail-closed(전부 거부) - fail-open보다 안전한 기본값
        if (expectedApiKey.isBlank()) {
            return false;
        }
        return expectedApiKey.equals(request.getHeader(HEADER_NAME));
    }
}
