package com.veloservice.auth.infraestructure.ratelimit;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory rate limiter for password reset requests.
 */
@Service
public class PasswordResetRateLimiter {
    private static final int MAX_REQUESTS = 3;
    private static final Duration WINDOW = Duration.ofHours(1);

    private final Map<String, Deque<Long>> emailBuckets = new ConcurrentHashMap<>();
    private final Map<String, Deque<Long>> ipBuckets = new ConcurrentHashMap<>();
    private final Object lock = new Object();

    public boolean allow(String email, String ip) {
        String emailKey = normalize(email);
        String ipKey = normalize(ip);
        long now = System.currentTimeMillis();

        synchronized (lock) {
            if (!canConsume(emailBuckets, emailKey, now) || !canConsume(ipBuckets, ipKey, now)) {
                return false;
            }
            consume(emailBuckets, emailKey, now);
            consume(ipBuckets, ipKey, now);
            return true;
        }
    }

    private boolean canConsume(Map<String, Deque<Long>> buckets, String key, long now) {
        if (!StringUtils.hasText(key)) {
            return true;
        }
        Deque<Long> deque = buckets.computeIfAbsent(key, k -> new ArrayDeque<>());
        prune(deque, now);
        return deque.size() < MAX_REQUESTS;
    }

    private void consume(Map<String, Deque<Long>> buckets, String key, long now) {
        if (!StringUtils.hasText(key)) {
            return;
        }
        Deque<Long> deque = buckets.computeIfAbsent(key, k -> new ArrayDeque<>());
        prune(deque, now);
        deque.addLast(now);
    }

    private void prune(Deque<Long> deque, long now) {
        long cutoff = now - WINDOW.toMillis();
        while (!deque.isEmpty() && deque.peekFirst() < cutoff) {
            deque.removeFirst();
        }
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim().toLowerCase() : null;
    }
}
