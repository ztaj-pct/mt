package com.pct.device.command.repository;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Repository;

@Repository
public class RedisDeviceCommandRepository {

	private RedisTemplate<String, Object> redisTemplate;
	private HashOperations hashOperations;
	private HashOperations<String,String,String> hashOperations1;
	

	@Autowired
	public RedisDeviceCommandRepository(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	@PostConstruct
	private void init() {
		hashOperations = redisTemplate.opsForHash();
		hashOperations1 = redisTemplate.opsForHash();
		
	}

	public void add(String deviceId, Map<String, String> values) {
		hashOperations.putAll(deviceId, values);
	}

	public List<String> findValuesForDevice(String deviceId, List<String> fields) {
		return hashOperations.multiGet(deviceId, fields);
	}
	public void  addMap(String key, String value)
	{
		hashOperations1.put(key, key, value);
	}
	
	
	public String getMap(String key)
	{
		return hashOperations1.get(key,key);
	}
	
	//Aamir
	public Long increment(String key) {
		RedisAtomicLong counter = new RedisAtomicLong(key, redisTemplate.getConnectionFactory());
		return counter.incrementAndGet();
	}
	
}
