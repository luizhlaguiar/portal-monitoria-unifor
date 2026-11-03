package com.unifor.monitoria.security;

import java.net.URI;

/** Valida URLs de anexo: so http(s) com host (bloqueia javascript:, data:, file: etc.). */
public final class UrlSafety {

    private UrlSafety() {}

    public static boolean isSafeHttpUrl(String url) {
        if (url == null || url.isBlank() || url.length() > 2000) return false;
        try {
            URI uri = new URI(url.trim());
            String scheme = uri.getScheme();
            return scheme != null
                    && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))
                    && uri.getHost() != null;
        } catch (Exception e) {
            return false;
        }
    }

    /** Campo opcional: vazio e aceito, preenchido precisa ser seguro. */
    public static boolean isSafeOrEmpty(String url) {
        return url == null || url.isBlank() || isSafeHttpUrl(url);
    }
}
