package sserver.util;

import java.util.concurrent.atomic.AtomicLong;

public class TsClock {
    private final AtomicLong counter = new AtomicLong(System.currentTimeMillis());

    public long next() {
        return counter.incrementAndGet();
    }

    public long current() {
        return counter.get();
    }
}