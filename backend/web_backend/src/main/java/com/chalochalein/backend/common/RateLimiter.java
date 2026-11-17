package com.chalochalein.backend.common;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimiter {
    private record Window(long start, AtomicInteger count) {}
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    public boolean allow(String key, int max, long windowMillis) {
        long now = System.currentTimeMillis();
        if (windows.size() > 50_000) windows.values().removeIf(w -> now - w.start() >= windowMillis);
        Window w = windows.compute(key, (k, old) ->
                (old == null || now - old.start() >= windowMillis) ? new Window(now, new AtomicInteger()) : old);
        return w.count().incrementAndGet() <= max;
    }
}
