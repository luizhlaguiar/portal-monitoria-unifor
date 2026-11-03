package com.unifor.monitoria.service;

import java.util.Base64;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Valida o conteudo das mensagens do chat.
 *
 * Texto comum passa. Anexos (conteudo que comeca com "data:" ou "[PDF:") so valem se forem
 * png, jpeg, gif, webp ou pdf em base64, e se os primeiros bytes baterem com o tipo declarado.
 * Isso bloqueia data:text/html, svg e outros tipos que executam script quando abertos.
 */
public final class MessageContentValidator {

    /** Tamanho maximo do conteudo, em caracteres (~6 MB de arquivo em base64). */
    public static final int MAX_CONTENT_CHARS = 8_000_000;

    private static final Set<String> ALLOWED_MIME = Set.of(
            "image/png", "image/jpeg", "image/gif", "image/webp", "application/pdf");

    private static final Pattern HEADER = Pattern.compile(
            "^(?:\\[PDF:[^\\]\\r\\n]{0,255}\\])?data:([a-z0-9.+\\-]+/[a-z0-9.+\\-]+);base64,",
            Pattern.CASE_INSENSITIVE);

    private MessageContentValidator() {
    }

    public static void validate(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("A mensagem nao pode ser vazia");
        }
        if (content.length() > MAX_CONTENT_CHARS) {
            throw new IllegalArgumentException("Mensagem muito grande (limite de cerca de 6 MB)");
        }

        String start = content.substring(0, Math.min(content.length(), 16)).toLowerCase();
        boolean attachment = start.startsWith("data:") || start.startsWith("[pdf:");
        if (!attachment) {
            return;
        }

        Matcher m = HEADER.matcher(content.substring(0, Math.min(content.length(), 600)));
        if (!m.find()) {
            throw new IllegalArgumentException("Anexo invalido");
        }
        String mime = m.group(1).toLowerCase();
        if (!ALLOWED_MIME.contains(mime)) {
            throw new IllegalArgumentException("Tipo de anexo nao permitido. Use png, jpeg, gif, webp ou pdf");
        }

        int payloadStart = m.end();
        int payloadLen = content.length() - payloadStart;
        if (payloadLen < 16) {
            throw new IllegalArgumentException("Anexo invalido");
        }
        for (int i = payloadStart; i < content.length(); i++) {
            char c = content.charAt(i);
            boolean ok = (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')
                    || c == '+' || c == '/' || c == '=';
            if (!ok) {
                throw new IllegalArgumentException("Anexo invalido");
            }
        }

        byte[] head;
        try {
            head = Base64.getDecoder().decode(content.substring(payloadStart, payloadStart + 16));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Anexo invalido");
        }
        if (!matchesMagic(mime, head)) {
            throw new IllegalArgumentException("O conteudo do anexo nao corresponde ao tipo declarado");
        }
    }

    private static boolean matchesMagic(String mime, byte[] b) {
        if (b.length < 12) {
            return false;
        }
        switch (mime) {
            case "image/png":
                return (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G';
            case "image/jpeg":
                return (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF;
            case "image/gif":
                return b[0] == 'G' && b[1] == 'I' && b[2] == 'F' && b[3] == '8';
            case "image/webp":
                return b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                        && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P';
            case "application/pdf":
                return b[0] == '%' && b[1] == 'P' && b[2] == 'D' && b[3] == 'F';
            default:
                return false;
        }
    }
}
