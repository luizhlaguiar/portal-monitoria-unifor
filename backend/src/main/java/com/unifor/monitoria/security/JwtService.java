package com.unifor.monitoria.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unifor.monitoria.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Emite e valida tokens JWT (HS256) usando apenas a biblioteca padrao do Java (sem dependencia nova).
 * O token so identifica o usuario (sub) e tem validade (exp). O papel NAO e confiado a partir do token:
 * o filtro sempre le o papel atual do banco.
 *
 * A chave vem da propriedade app.jwt.secret (variavel de ambiente JWT_SECRET, minimo 32 caracteres).
 * Se nao for definida, uma chave aleatoria e gerada a cada subida (os tokens deixam de valer ao reiniciar).
 */
@Component
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder B64D = Base64.getUrlDecoder();
    private static final String HEADER =
            B64.encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));

    private final byte[] key;
    private final long ttlSeconds;

    public JwtService(@Value("${app.jwt.secret:}") String secret,
                      @Value("${app.jwt.ttl-hours:8}") long ttlHours) {
        if (secret == null || secret.trim().length() < 32) {
            byte[] random = new byte[48];
            new SecureRandom().nextBytes(random);
            this.key = random;
            log.warn("JWT_SECRET nao definido (ou menor que 32 caracteres): usando chave aleatoria. "
                    + "Os logins serao invalidados a cada reinicio do backend.");
        } else {
            this.key = secret.trim().getBytes(StandardCharsets.UTF_8);
        }
        this.ttlSeconds = Math.max(1, ttlHours) * 3600;
    }

    public String generate(User user) {
        try {
            long now = Instant.now().getEpochSecond();
            Map<String, Object> claims = new LinkedHashMap<>();
            claims.put("sub", String.valueOf(user.getId()));
            claims.put("iat", now);
            claims.put("exp", now + ttlSeconds);
            String payload = B64.encodeToString(MAPPER.writeValueAsBytes(claims));
            String unsigned = HEADER + "." + payload;
            return unsigned + "." + B64.encodeToString(hmac(unsigned));
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao gerar token", e);
        }
    }

    /** Devolve o id do usuario se o token for valido (assinatura correta e nao expirado). */
    public Optional<Integer> parseUserId(String token) {
        try {
            if (token == null) return Optional.empty();
            String[] parts = token.trim().split("\\.");
            if (parts.length != 3) return Optional.empty();

            byte[] expected = hmac(parts[0] + "." + parts[1]);
            byte[] given = B64D.decode(parts[2]);
            if (!MessageDigest.isEqual(expected, given)) return Optional.empty();

            JsonNode header = MAPPER.readTree(B64D.decode(parts[0]));
            if (!"HS256".equals(header.path("alg").asText())) return Optional.empty();

            JsonNode payload = MAPPER.readTree(B64D.decode(parts[1]));
            long exp = payload.path("exp").asLong(0);
            if (exp <= Instant.now().getEpochSecond()) return Optional.empty();

            return Optional.of(Integer.parseInt(payload.path("sub").asText()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private byte[] hmac(String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }
}
