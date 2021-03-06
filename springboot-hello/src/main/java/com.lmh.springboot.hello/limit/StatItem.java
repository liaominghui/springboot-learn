package com.lmh.springboot.hello.limit;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 统计项
 */
class StatItem {

    /**
     * 统计名，目前使用服务名
     */
    private String name;
    /**
     * 周期
     */
    private long interval;
    /**
     * 限制大小
     */
    private int rate;
    /**
     * 最后重置时间
     */
    private long lastResetTime;
    /**
     * 当前周期，剩余种子数
     */
    private AtomicInteger token;

    StatItem(String name, int rate, long interval) {
        this.name = name;
        this.rate = rate;
        this.interval = interval;
        this.lastResetTime = System.currentTimeMillis();
        this.token = new AtomicInteger(rate);
    }

    public boolean isAllowable(String serviceKey, int rate, long interval) {
        // 若到达下一个周期，恢复可用种子数，设置最后重置时间。
        long now = System.currentTimeMillis();
        if (now > lastResetTime + interval) {
            token.set(rate); // 回复可用种子数
            lastResetTime = now; // 最后重置时间
        }

        // CAS ，直到或得到一个种子，或者没有足够种子
        int value = token.get();
        boolean flag = false;
        while (value > 0 && !flag) {
            flag = token.compareAndSet(value, value - 1);
            value = token.get();
        }

        // 是否成功
        return flag;
    }

    long getLastResetTime() {
        return lastResetTime;
    }

    int getToken() {
        return token.get();
    }

    public String toString() {
        return new StringBuilder(32).append("StatItem ")
                .append("[name=").append(name).append(", ")
                .append("rate = ").append(rate).append(", ")
                .append("interval = ").append(interval).append("]")
                .toString();
    }

}
