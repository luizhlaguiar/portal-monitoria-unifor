package com.unifor.monitoria.config;

import com.unifor.monitoria.security.AuthUser;
import com.unifor.monitoria.security.CurrentUser;
import com.unifor.monitoria.security.TokenAuthenticator;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

/**
 * Autentica e autoriza o STOMP:
 * - CONNECT exige o cabecalho Authorization com um token valido (vira o usuario da sessao);
 * - SUBSCRIBE so e aceito no proprio canal /topic/chat.{meuId};
 * - SEND so e aceito para /app/chat.send.
 */
@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private static final String CHAT_TOPIC_PREFIX = "/topic/chat.";
    private static final String SEND_DESTINATION = "/app/chat.send";

    private final TokenAuthenticator authenticator;

    public WebSocketAuthInterceptor(TokenAuthenticator authenticator) {
        this.authenticator = authenticator;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }
        StompCommand command = accessor.getCommand();

        if (StompCommand.CONNECT.equals(command)) {
            AuthUser user = authenticator.authenticate(accessor.getFirstNativeHeader("Authorization"))
                    .orElseThrow(() -> new MessagingException("Nao autenticado"));
            accessor.setUser(TokenAuthenticator.toAuthentication(user));
            return message;
        }

        if (StompCommand.SUBSCRIBE.equals(command)) {
            AuthUser user = CurrentUser.fromPrincipal(accessor.getUser());
            String destination = accessor.getDestination();
            if (user == null || destination == null
                    || !destination.equals(CHAT_TOPIC_PREFIX + user.id())) {
                throw new MessagingException("Inscricao nao permitida");
            }
            return message;
        }

        if (StompCommand.SEND.equals(command)) {
            AuthUser user = CurrentUser.fromPrincipal(accessor.getUser());
            if (user == null || !SEND_DESTINATION.equals(accessor.getDestination())) {
                throw new MessagingException("Envio nao permitido");
            }
        }
        return message;
    }
}
