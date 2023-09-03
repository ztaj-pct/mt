package com.pct.common.redis;

public interface KeyPrefix {
    /**
     * Validity period
     * @return
     */
    public int expireSeconds();

    /**
     * Key prefix to prevent other people from overwriting when using redis
     * @return
     */
    public String getPrefix();
}
