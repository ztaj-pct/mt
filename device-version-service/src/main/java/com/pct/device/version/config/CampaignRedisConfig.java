package com.pct.device.version.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class CampaignRedisConfig {
	Logger logger = LoggerFactory.getLogger(CampaignRedisConfig.class);
	@Value("${spring.redis.campaign.host}")
	private String redisCampaignHost;
	@Value("${spring.redis.campaign.port}")
	private int redisCampaignPort;
    
	@Bean
	JedisConnectionFactory CampJedisConnectionFactory() {
		logger.info(" In side CampJedisConnectionFactory> ");

		JedisConnectionFactory jedisConFactory = null;
		try {
			RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(redisCampaignHost,
					redisCampaignPort);
			jedisConFactory = new JedisConnectionFactory(redisStandaloneConfiguration);
			jedisConFactory.getPoolConfig().setMaxTotal(50);
			jedisConFactory.getPoolConfig().setMaxIdle(0);
			logger.info("CampJedisConnectionFactory port: "+redisCampaignPort+" host: "+redisCampaignHost);

		} catch (RedisConnectionFailureException e) {
			logger.info(" Catch block CampJedisConnectionFactory ");

			e.getMessage();
		}
		return jedisConFactory;
	}


    @Bean(name = "campaignRedisTemplate")
    public RedisTemplate<String, Object> campaignRedisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(CampJedisConnectionFactory());
        template.setDefaultSerializer(campStringRedisSerializer());
        //template.setValueSerializer(new GenericToStringSerializer<Object>(Object.class));
        //template.afterPropertiesSet();
        return template;
    }
    
    @Bean
    public StringRedisSerializer campStringRedisSerializer() {
        return new StringRedisSerializer();
    }
}
