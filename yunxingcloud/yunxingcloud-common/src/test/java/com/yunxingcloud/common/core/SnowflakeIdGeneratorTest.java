package com.yunxingcloud.common.core;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SnowflakeIdGeneratorTest {

    @Test
    void nextIdShouldReturnPositiveLong() {
        SnowflakeIdGenerator gen = new SnowflakeIdGenerator(1, 1);
        long id = gen.nextId();
        assertTrue(id > 0);
    }

    @Test
    void nextIdShouldReturnUniqueValues() {
        SnowflakeIdGenerator gen = new SnowflakeIdGenerator(1, 1);
        Set<Long> ids = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            ids.add(gen.nextId());
        }
        assertEquals(1000, ids.size());
    }

    @Test
    void nextIdShouldBeMonotonicallyIncreasing() {
        SnowflakeIdGenerator gen = new SnowflakeIdGenerator(1, 1);
        long prev = gen.nextId();
        for (int i = 0; i < 100; i++) {
            long curr = gen.nextId();
            assertTrue(curr > prev, "ID should increase: " + prev + " -> " + curr);
            prev = curr;
        }
    }

    @Test
    void validWorkerIdShouldNotThrow() {
        assertDoesNotThrow(() -> new SnowflakeIdGenerator(0, 0));
        assertDoesNotThrow(() -> new SnowflakeIdGenerator(31, 5));
    }

    @Test
    void negativeWorkerIdShouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> new SnowflakeIdGenerator(-1, 0));
    }

    @Test
    void tooLargeWorkerIdShouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> new SnowflakeIdGenerator(32, 0));
    }

    @Test
    void differentGeneratorsShouldProduceDifferentIds() {
        SnowflakeIdGenerator g1 = new SnowflakeIdGenerator(1, 1);
        SnowflakeIdGenerator g2 = new SnowflakeIdGenerator(2, 1);
        long id1 = g1.nextId();
        long id2 = g2.nextId();
        assertNotEquals(id1, id2);
    }
}