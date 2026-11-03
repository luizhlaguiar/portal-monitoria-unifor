package com.unifor.monitoria.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Le o cabecalho "Authorization: Bearer <token>" e, se valido, autentica a requisicao.
 * Sem token (ou token invalido) a requisicao segue sem autenticacao e o Spring Security responde 401.
 * Nao e um @Component de proposito: so entra na cadeia do Spring Security (veja SecurityConfig).
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private final TokenAuthenticator authenticator;

    public JwtAuthFilter(TokenAuthenticator authenticator) {
        this.authenticator = authenticator;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.regionMatches(true, 0, "Bearer ", 0, 7)) {
            authenticator.authenticate(header).ifPresent(user ->
                    SecurityContextHolder.getContext().setAuthentication(TokenAuthenticator.toAuthentication(user)));
        }
        chain.doFilter(request, response);
    }
}
