package io.github.jaspeen.ulid;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

class MonotonicULIDTest {

    @Test
    void next() {
        MonotonicULID u = new MonotonicULID(ThreadLocalRandom.current());
        ULID u1 = u.next();
        ULID u2 = u.next();

        assertEquals(u1.compareTo(u2), -1);
    }

    @Test
    void random() {
        ULID u1 = MonotonicULID.random();
        ULID u2 = MonotonicULID.random();

        assertEquals(u1.compareTo(u2), -1);
    }
}