package com.ebanx.challenge.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SecurityHeadersFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletResponse res = (HttpServletResponse) response;

        chain.doFilter(request, response); // 👈 primeiro processa

        // 👇 depois aplica headers (inclusive em erro)
        res.setHeader("X-Content-Type-Options", "nosniff");
        res.setHeader("X-Frame-Options", "DENY");
        res.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        res.setHeader("Cross-Origin-Resource-Policy", "same-origin");
        res.setHeader("Content-Security-Policy", "default-src 'self'");
    }
}