package com.yunxingcloud.common.core;

/**
 * 雪花算法分布式ID生成器
 */
public final class SnowflakeIdGenerator {

    private final long epoch = 1700000000000L;
    private final long workerIdBits = 5L, datacenterIdBits = 5L;
    private final long maxWorkerId = ~(-1L << workerIdBits);
    private final long sequenceBits = 12L;
    private final long workerIdShift = sequenceBits;
    private final long datacenterIdShift = sequenceBits + workerIdBits;
    private final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;
    private final long sequenceMask = ~(-1L << sequenceBits);

    private long workerId, datacenterId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    public SnowflakeIdGenerator(long workerId, long datacenterId) {
        if (workerId > maxWorkerId || workerId < 0) throw new IllegalArgumentException("workerId范围: 0-" + maxWorkerId);
        this.workerId = workerId; this.datacenterId = datacenterId;
    }

    public synchronized long nextId() {
        long timestamp = System.currentTimeMillis();
        if (timestamp < lastTimestamp) throw new RuntimeException("时钟回拨");
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) timestamp = tilNextMillis(lastTimestamp);
        } else sequence = 0L;
        lastTimestamp = timestamp;
        return ((timestamp - epoch) << timestampLeftShift)
                | (datacenterId << datacenterIdShift)
                | (workerId << workerIdShift)
                | sequence;
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) timestamp = System.currentTimeMillis();
        return timestamp;
    }
}