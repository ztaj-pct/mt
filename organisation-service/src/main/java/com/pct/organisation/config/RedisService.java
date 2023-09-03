package com.pct.organisation.config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.pct.common.redis.KeyPrefix;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
public class RedisService {
    final JedisPool jedisPool;

    public RedisService(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    /**
     * Obtain a redis instance from the redis connection pool
     * @param prefix
     * @param key
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T get(KeyPrefix prefix,String key, Class<T> clazz){
        Jedis jedis = new Jedis();
        try {
            jedis = jedisPool.getResource();
            //Add a prefix to the key, which can be used for classification to avoid key duplication
            String realKey=prefix.getPrefix() + key;
            String str = jedis.get(realKey);

            T t = stringToBean(str,clazz);
            return t;
        }finally {
            returnToPool(jedis);
        }
    }

    /**
     * Obtain list of redis instance from the redis connection pool
     * @param <T>
     * @param prefix
     * @param key
     * @param clazz
     * @return
     */
    public <T> List<T> hgetall(KeyPrefix prefix, String key, Class<T> clazz){
        Jedis jedis = new Jedis();
        try {
            jedis = jedisPool.getResource();
            //Add a prefix to the key, which can be used for classification to avoid key duplication
            String realKey=prefix.getPrefix() + key;
            Map<String, String> map = jedis.hgetAll(realKey);
            List<T> ts = new ArrayList<>();
            if (map != null) {
                Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
                while(iterator.hasNext()) {
                    Map.Entry<String, String> entry = iterator.next();
                    T t = stringToBean(entry.getValue(), clazz);
                    ts.add(t);
                }
                return ts;
            } else {
                return null;
            }
        }finally {
            returnToPool(jedis);
        }
    }
    
    /**
     * Obtain list of redis instance from the redis connection pool
     * @param <T>
     * @param prefix
     * @param clazz
     * @return
     */
    public <T> List<T> hgetall(KeyPrefix prefix, Class<T> clazz){
        Jedis jedis = new Jedis();
        try {
            jedis = jedisPool.getResource();
            //Add a prefix to the key, which can be used for classification to avoid key duplication
            String realKey=prefix.getPrefix();
            Map<String, String> map = jedis.hgetAll(realKey);
            List<T> ts = new ArrayList<>();
            if (map != null) {
                Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
                while(iterator.hasNext()) {
                    Map.Entry<String, String> entry = iterator.next();
                    T t = stringToBean(entry.getValue(), clazz);
                    ts.add(t);
                }
                return ts;
            } else {
                return null;
            }
        }finally {
            returnToPool(jedis);
        }
    }

    /**
     * Storage object
     * @param prefix
     * @param key
     * @param value
     * @param <T>
     * @return
     */
    public <T> Boolean set(KeyPrefix prefix,String key,T value){
        Jedis jedis = new Jedis();
        try {
            jedis = jedisPool.getResource();
            String str = beanToString(value);
            if (str == null || str.length() <=0){
                return false;
            }
            String realKey = prefix.getPrefix() + key;
            int seconds = prefix.expireSeconds(); //Get expiration time
            if (seconds <= 0 ){
                jedis.set(realKey,str);
            }else {
                jedis.setex(realKey,seconds,str);
            }
            return true;
        }finally {
            returnToPool(jedis);
        }
    }

    /**
     * Delete
     * @param prefix
     * @param key
     * @return
     */
    public boolean delete(KeyPrefix prefix,String key){
        Jedis jedis = new Jedis();
        try {
            jedis = jedisPool.getResource();
            String realKey = prefix.getPrefix() + key;
            long ret = jedis.del(realKey);
            return ret>0;
        }finally {
            returnToPool(jedis);
        }
    }

    /**
     * Determine whether the key exists
     * @param prefix
     * @param key
     * @param <T>
     * @return
     */
    public <T> boolean exists(KeyPrefix prefix,String key){
        Jedis jedis = new Jedis();
        try {
            jedis = jedisPool.getResource();
            String realKey = prefix.getPrefix() + key;
            return jedis.exists(realKey);
        }finally {
            returnToPool(jedis);
        }
    }

    /**
     * Determine whether the value corresponding to the key exists
     * @param prefix
     * @param key
     * @param field
     * @return
     */
    public boolean existsValue(KeyPrefix prefix,String key,String field){
        Jedis jedis = new Jedis();
        try {
            jedis = jedisPool.getResource();
            String realkey = prefix.getPrefix() + key;
            Boolean result = jedis.hexists(realkey,field);
            return result;
        }finally {
            returnToPool(jedis);
        }
    }

    /**
     * Value Added
     * @param prefix
     * @param key
     * @param <T>
     * @return
     */
    public <T> Long incr(KeyPrefix prefix,String key){
        Jedis jedis = new Jedis();
        try {
            jedis = jedisPool.getResource();
            String realKey = prefix.getPrefix()+key;
            return jedis.incr(realKey);
        }finally {
            returnToPool(jedis);
        }
    }

    /**
     * Decrease value
     * @param prefix
     * @param key
     * @param <T>
     * @return
     */
    public <T> Long decr(KeyPrefix prefix,String key){
        Jedis jedis = new Jedis();
        try {
            jedis = jedisPool.getResource();
            String realKey = prefix.getPrefix()+key;
            return jedis.decr(realKey);
        }finally {
            returnToPool(jedis);
        }
    }

    /**
     * Return the value of the specified field
     * @param prefix
     * @param key
     * @param filed
     * @param <T>
     * @return
     */
    public <T> String hget(KeyPrefix prefix,String key,String filed){
        Jedis jedis = new Jedis();
        try {
            jedis = jedisPool.getResource();
            String realKey = prefix.getPrefix()+key;
            return jedis.hget(realKey,filed);
        }finally {
            returnToPool(jedis);
        }
    }
    
    /**
     * Return the value of the specified field
     * @param prefix
     * @param filed
     * @param <T>
     * @return
     */
    public <T> String hget(KeyPrefix prefix,String filed){
        Jedis jedis = new Jedis();
        try {
            jedis = jedisPool.getResource();
            String realKey = prefix.getPrefix();
            return jedis.hget(realKey,filed);
        }finally {
            returnToPool(jedis);
        }
    }

    /**
     *
     * @param prefix
     * @param key
     * @param field
     * @param value
     * @param <T>
     * @return
     */
    public<T> Long hset(KeyPrefix prefix,String key,String field,String value){
        Jedis jedis = new Jedis();
        try {
            jedis = jedisPool.getResource();
            String realKey = prefix.getPrefix()+key;
            return jedis.hset(realKey,field,value);
        }finally {
            returnToPool(jedis);
        }
    }
    
    /**
    *
    * @param prefix
    * @param field
    * @param value
    * @param <T>
    * @return
    */
    public<T> Long hset(KeyPrefix prefix,String field,String value){
        Jedis jedis = new Jedis();
        try {
            jedis = jedisPool.getResource();
            String realKey = prefix.getPrefix();
            return jedis.hset(realKey,field,value);
        }finally {
            returnToPool(jedis);
        }
    }
    
    public<T> Long lset(KeyPrefix prefix,String field,String value){
        Jedis jedis = new Jedis();
        try {
            jedis = jedisPool.getResource();
            String realKey = prefix.getPrefix();
            return jedis.lpush(realKey,field,value);
        }finally {
            returnToPool(jedis);
        }
    }

    /**
     * Get the list value
     * @param prefix
     * @param key
     * @return
     */
    public List<String> hvals(KeyPrefix prefix,String key){
        Jedis jedis = new Jedis();
        try {
            jedis = jedisPool.getResource();
            String realKey = prefix.getPrefix()+key;
            return jedis.hvals(realKey);
        }finally {
            returnToPool(jedis);
        }
    }
    
    public List<String> hvals(KeyPrefix prefix){
        Jedis jedis = new Jedis();
        try {
            jedis = jedisPool.getResource();
            String realKey = prefix.getPrefix();
            return jedis.hvals(realKey);
        }finally {
            returnToPool(jedis);
        }
    }

    /**
     * Delete value
     * @param prefix
     * @param key
     * @param field
     * @return
     */
    public Long hdel(KeyPrefix prefix,String key,String field){
        Jedis jedis = new Jedis();
        try {
            jedis = jedisPool.getResource();
            String realKey = prefix.getPrefix()+key;
            return jedis.hdel(realKey,field);
        }finally {
            returnToPool(jedis);
        }
    }



    public static <T> String beanToString(T value){
        if (value ==null){
            return null;
        }
        Class<?> clazz = value.getClass();
        if (clazz ==int.class || clazz ==Integer.class){
            return String.valueOf(value);
        }else if(clazz ==long.class || clazz == Long.class){
            return String.valueOf(value);
        }else if (clazz ==String.class){
            return (String) value;
        }else{
            return JSON.toJSONString(value);
        }
    }

    // Convert string type to entity class
    public static <T> T stringToBean(String str,Class<T> clazz){
        if (str == null || str.length() <=0 || clazz==null){
            return null;
        }
        if (clazz ==int.class || clazz == Integer.class){
            return (T) Integer.valueOf(str);
        }else if(clazz == long.class || clazz ==Long.class){
            return (T) Long.valueOf(str);
        }else if (clazz == String.class){
            return (T) str;
        }else {
            return JSON.toJavaObject(JSON.parseObject(str),clazz);
        }
    }


    private void returnToPool(Jedis jedis){
        if(jedis != null){
            jedis.close();
        }
    }
    
    /**
     * Get keys
     * @param prefix
     * @param start
     * @param end
     * @return List of String
     */
	public Set<String> getKeys(String pattern) {
		Jedis jedis = new Jedis();
		try {
			jedis = jedisPool.getResource();
			return jedis.keys(pattern);
		} finally {
			returnToPool(jedis);
		}
	}
}
