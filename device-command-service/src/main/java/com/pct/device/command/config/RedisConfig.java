package com.pct.device.command.config;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import ch.qos.logback.classic.Logger;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class RedisConfig {

	@Value("${redis.host}")
	private String redisHost;
	@Value("${redis.port}")
	private int redisPort;

	@Bean
	JedisConnectionFactory jedisConnectionFactory() throws Exception {
		JedisConnectionFactory jedisConFactory = null;
		System.out.println("Connecting to redis **** " + redisHost);
		RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(redisHost,
				redisPort);
		jedisConFactory = new JedisConnectionFactory(redisStandaloneConfiguration);
		jedisConFactory.getPoolConfig().setMaxTotal(50);
		jedisConFactory.getPoolConfig().setMaxIdle(5);
		if (jedisConFactory == null) {
			throw new Exception("Redis Connection Has Not Established");
		}

		return jedisConFactory;
	}

	@Bean
	public RedisTemplate<String, Object> redisTemplate() throws Exception {
		System.out.println("Connecting to redis **** " + redisHost);
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(jedisConnectionFactory());
		template.setDefaultSerializer(stringRedisSerializer());
		return template;
	}

	@Bean
	public StringRedisSerializer stringRedisSerializer() {
		return new StringRedisSerializer();
	}

}
