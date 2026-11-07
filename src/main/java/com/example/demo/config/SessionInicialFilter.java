package com.example.demo.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Garantiza que exista una sesión antes de que la respuesta HTML empiece a
 * escribirse. Así Spring Security puede generar el token CSRF que inyecta
 * Thymeleaf en los formularios, incluso en páginas que superan el búfer de
 * respuesta del servidor.
 */
@Component
public class SessionInicialFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest) {
            String uri = httpRequest.getRequestURI();
            boolean recursoEstatico = uri.startsWith("/css/")
                    || uri.startsWith("/js/")
                    || uri.startsWith("/images/")
                    || uri.startsWith("/webjars/")
                    || uri.equals("/favicon.ico");
            if (!recursoEstatico) {
                httpRequest.getSession(true);
            }
        }
        chain.doFilter(request, response);
    }
}
