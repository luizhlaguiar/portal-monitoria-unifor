package com.unifor.monitoria.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

/** Acesso ao usuario autenticado da requisicao atual. */
public final class CurrentUser {

    private CurrentUser() {}

    public static AuthUser require() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthUser user) {
            return user;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nao autenticado");
    }

    /** Usuario a partir do Principal de uma sessao STOMP (definido no CONNECT). */
    public static AuthUser fromPrincipal(Principal principal) {
        if (principal instanceof Authentication auth && auth.getPrincipal() instanceof AuthUser user) {
            return user;
        }
        return null;
    }
}
