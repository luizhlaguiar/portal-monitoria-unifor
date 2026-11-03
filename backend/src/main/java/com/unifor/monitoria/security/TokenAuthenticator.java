package com.unifor.monitoria.security;

import com.unifor.monitoria.dao.UserDao;
import com.unifor.monitoria.model.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** Transforma um token em usuario autenticado (usado pelo filtro HTTP e pelo WebSocket). */
@Component
public class TokenAuthenticator {

    private final JwtService jwtService;
    private final UserDao userDao;

    public TokenAuthenticator(JwtService jwtService, UserDao userDao) {
        this.jwtService = jwtService;
        this.userDao = userDao;
    }

    /** Aceita o valor cru do token ou o cabecalho completo ("Bearer xxx"). */
    public Optional<AuthUser> authenticate(String headerOrToken) {
        if (headerOrToken == null) return Optional.empty();
        String token = headerOrToken.trim();
        if (token.regionMatches(true, 0, "Bearer ", 0, 7)) {
            token = token.substring(7).trim();
        }
        return jwtService.parseUserId(token)
                .flatMap(userDao::findById)
                .map(TokenAuthenticator::toAuthUser);
    }

    private static AuthUser toAuthUser(User u) {
        return new AuthUser(u.getId(), u.getRole() != null ? u.getRole().name() : "ALUNO");
    }

    public static Authentication toAuthentication(AuthUser user) {
        return new UsernamePasswordAuthenticationToken(
                user, null, List.of(new SimpleGrantedAuthority("ROLE_" + user.role())));
    }
}
