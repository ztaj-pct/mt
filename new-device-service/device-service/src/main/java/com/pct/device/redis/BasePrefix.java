package com.pct.device.redis;

public abstract class BasePrefix implements KeyPrefix {
    private int expireSeconds;

    private String prefix;

    // 0 means no expiration
    public BasePrefix(String prefix){
        this(0,prefix);
    }

    public BasePrefix(int expireSeconds,String prefix){
        this.expireSeconds = expireSeconds;
        this.prefix = prefix;
    }

    @Override
    public int expireSeconds(){
        return expireSeconds;
    }

    @Override
    public String getPrefix(){
        String className = getClass().getSimpleName();// Get the parameter class name
        return className+":"+prefix;
    }
}
