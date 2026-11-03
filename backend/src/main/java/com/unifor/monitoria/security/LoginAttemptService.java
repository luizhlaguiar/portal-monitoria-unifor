package com.unifor.monitoria.security;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limita tentativas de login (forca bruta), em memoria.
 * - por IP + matricula: 5 falhas em 15 min bloqueiam por 15 min;
 * - por IP: 30 falhas em 15 min bloqueiam por 15 min (varrer varias matriculas).
 * Login correto zera o contador da dupla IP + matricula.
 */
@Component
public class LoginAttemptService {

    private static final Duration WINDOW = Duration.ofMinutes(15);
    private static final Duration LOCK = Duration.ofMinutes(15);
    private static final int MAX_PER_ACCOUNT = 5;
    private static final int MAX_PER_IP = 30;

    private static final class Entry {
        int failures;
        Instant windowStart = Instant.now();
        Instant lockedUntil;
    }

    private final Map<String, Entry> entries = new ConcurrentHashMap<>();

    private static String accountKey(String ip, String matricula) {
        return "a|" + ip + "|" + (matricula == null ? "" : matricula.trim().toLowerCase());
    }

    private static String ipKey(String ip) {
        return "i|" + ip;
    }

    /** Segundos restantes de bloqueio (0 = liberado). */
    public long secondsBlocked(String ip, String matricula) {
        return Math.max(remaining(accountKey(ip, matricula)), remaining(ipKey(ip)));
    }

    public void recordFailure(String ip, String matricula) {
        purgeIfLarge();
        register(accountKey(ip, matricula), MAX_PER_ACCOUNT);
        register(ipKey(ip), MAX_PER_IP);
    }

    public void recordSuccess(String ip, String matricula) {
        entries.remove(accountKey(ip, matricula));
    }

    private long remaining(String key) {
        Entry e = entries.get(key);
        if (e == null || e.lockedUntil == null) return 0;
        long s = Duration.between(Instant.now(), e.lockedUntil).getSeconds();
        return Math.max(s, 0);
    }

    private void register(String key, int max) {
        entries.compute(key, (k, e) -> {
            Instant now = Instant.now();
            if (e == null || now.isAfter(e.windowStart.plus(WINDOW))
                    || (e.lockedUntil != null && now.isAfter(e.lockedUntil))) {
                e = new Entry();
            }
            e.failures++;
            if (e.failures >= max) {
                e.lockedUntil = now.plus(LOCK);
            }
            return e;
        });
    }

    private void purgeIfLarge() {
        if (entries.size() < 10_000) return;
        Instant now = Instant.now();
        entries.entrySet().removeIf(en -> now.isAfter(en.getValue().windowStart.plus(WINDOW))
                && (en.getValue().lockedUntil == null || now.isAfter(en.getValue().lockedUntil)));
    }
}
