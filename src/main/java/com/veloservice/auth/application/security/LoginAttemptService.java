package com.veloservice.auth.application.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {
    private final int maxAttempts;
    private final Duration lockDuration;
    private final Duration windowDuration;
    private final ConcurrentHashMap<String, Attempt> attempts = new ConcurrentHashMap<>();

    public LoginAttemptService(
            @Value("${security.login.max-attempts:5}") int maxAttempts,
            @Value("${security.login.lock-minutes:15}") long lockMinutes,
            @Value("${security.login.window-minutes:10}") long windowMinutes) {
        this.maxAttempts = Math.max(maxAttempts, 1);
        this.lockDuration = Duration.ofMinutes(Math.max(lockMinutes, 1));
        this.windowDuration = Duration.ofMinutes(Math.max(windowMinutes, 1));
    }

    public boolean isBlocked(String email) {
        String key = normalize(email);
        Attempt attempt = attempts.get(key);
        if (attempt == null) {
            return false;
        }

        Instant now = Instant.now();
        if (attempt.lockedUntil != null) {
            if (now.isBefore(attempt.lockedUntil)) {
                return true;
            }
            attempts.remove(key);
            return false;
        }

        if (now.isAfter(attempt.lastAttempt.plus(windowDuration))) {
            attempts.remove(key);
            return false;
        }

        return attempt.count >= maxAttempts;
    }

    public void recordFailedAttempt(String email) {
        String key = normalize(email);
        attempts.compute(key, (k, existing) -> {
            Instant now = Instant.now();

            if (existing == null
                    || now.isAfter(existing.lastAttempt.plus(windowDuration))
                    || (existing.lockedUntil != null && now.isAfter(existing.lockedUntil))) {
                existing = new Attempt(0, now, null);
            }

            int nextCount = existing.count + 1;
            Instant lockedUntil = existing.lockedUntil;
            if (nextCount >= maxAttempts) {
                lockedUntil = now.plus(lockDuration);
            }

            return new Attempt(nextCount, now, lockedUntil);
        });
    }

    public void resetAttempts(String email) {
        attempts.remove(normalize(email));
    }

    private String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private static final class Attempt {
        private final int count;
        private final Instant lastAttempt;
        private final Instant lockedUntil;

        private Attempt(int count, Instant lastAttempt, Instant lockedUntil) {
            this.count = count;
            this.lastAttempt = lastAttempt;
            this.lockedUntil = lockedUntil;
        }
    }
}
