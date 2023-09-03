package com.pct.organisation.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Service
public class RedisPoolFactory {

    @Autowired
    RedisConfiguration redisConfiguration;

    @Bean
    public JedisPool jedisPoolFactory(){
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxIdle(redisConfiguration.getPoolMaxIdle());
        config.setMaxTotal(redisConfiguration.getPoolMaxTotal());
        config.setMaxWaitMillis(redisConfiguration.getPoolMaxWait() * 1000);
        JedisPool jp = new JedisPool(config, redisConfiguration.getHost(), redisConfiguration.getPort(),
                redisConfiguration.getTimeout()*1000);
        return jp;
    }
}
