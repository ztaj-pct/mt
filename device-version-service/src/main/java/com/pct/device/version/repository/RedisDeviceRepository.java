package com.pct.device.version.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/**
 * @author Abhishek on 19/03/21
 */

@Repository
public class RedisDeviceRepository {
	Logger logger = LoggerFactory.getLogger(RedisDeviceRepository.class);
    private RedisTemplate<String, Object> redisTemplate;
    private HashOperations hashOperations;

    @Autowired
    public RedisDeviceRepository(RedisTemplate<String, Object> redisTemplate){
 
		logger.info(" In side RedisDeviceRepository constructor KEY_NAME>>>: "+redisTemplate.getConnectionFactory().getConnection().incr("stagingRedis".getBytes()));

        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    private void init(){
        hashOperations = redisTemplate.opsForHash();
    }

    public void add(String deviceId, Map<String, String> values) {
        hashOperations.putAll(deviceId, values);
    }

    public List<String> findValuesForDevice(String deviceId, List<String>fields){
		logger.info(" findValuesForDevice: deviceId:: "+deviceId+" fields: "+fields);

        return hashOperations.multiGet(deviceId, fields);
    }
}
