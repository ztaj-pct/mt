package com.pct.device.repository;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RedisDeviceRepository {

	private RedisTemplate<String, Object> redisTemplate;
	private HashOperations hashOperations;
	private HashOperations<String,String,String> hashOperations1;
	

	@Autowired
	public RedisDeviceRepository(RedisTemplate<String, Object> redisTemplate) {
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
	
	public void  addMap(String key, String innerkey , String value)
	{
		hashOperations1.put(key, innerkey, value);
	}
	
	
	public String getMap(String key,String innerkey )
	{
		return hashOperations1.get(key,innerkey);
	}
	
	public long removeMap(String key, String innerkey)
	{
		return hashOperations1.delete(key, innerkey);
	}

	public Map<String, String> getValue(String key)
	{
		Map<String, String> entries = hashOperations1.entries(key);
		return entries;
	}
}
