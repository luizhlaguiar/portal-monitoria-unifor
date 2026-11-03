package com.unifor.monitoria.security;

/** Usuario autenticado da requisicao (id e papel lidos do banco, nunca do cliente). */
public record AuthUser(Integer id, String role) {

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    public boolean isMonitor() {
        return "MONITOR".equals(role);
    }
}
