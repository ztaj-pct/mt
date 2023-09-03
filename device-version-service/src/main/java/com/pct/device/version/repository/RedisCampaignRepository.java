package com.pct.device.version.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;


@Repository
public class RedisCampaignRepository {
	Logger logger = LoggerFactory.getLogger(RedisCampaignRepository.class);
	Logger analysisLog = LoggerFactory.getLogger("analytics");
	@Autowired
	@Qualifier("campaignRedisTemplate")
    private RedisTemplate<String, Object> campaignRedisTemplate;
    private HashOperations hashOperations;

    @Autowired
    public RedisCampaignRepository(@Qualifier("campaignRedisTemplate") RedisTemplate<String, Object> campaignRedisTemplate){
		logger.info(" In side RedisCampaignRepository constructor KEY_NAME>>>>>>>>>>>>>>>>>>>>>: "+campaignRedisTemplate.getConnectionFactory().getConnection().incr("qaRedis".getBytes()));
 
    //	campaignRedisTemplate.getConnectionFactory().getConnection().getClientName();
    	this.campaignRedisTemplate = campaignRedisTemplate;
    }

    @PostConstruct
    private void init(){
        hashOperations = campaignRedisTemplate.opsForHash();
    }

    public void add(String deviceId, Map<String, String> values) {
        hashOperations.putAll(deviceId, values);
    }

    public List<String> findValuesForDevice(String deviceId, List<String>fields){
		logger.info(" In side findValuesForDevice  deviceId: "+deviceId+" fields: "+fields.toString());

        // Check size of fields List
        /* Part of invalid bulk exception: translates into hMGet redis command*/
        return hashOperations.multiGet(deviceId, fields);
        // Internet research says this exception is thrown when args in an operation are greater than 1024 * 1024 (524287)
    }
    
    
    public List<String> findValuesForDeviceScheduler(String deviceId, List<String>fields){
    	analysisLog.info(" In side findValuesForDevice  deviceId: "+deviceId+" fields: "+fields.toString());

          // Check size of fields List
          /* Part of invalid bulk exception: translates into hMGet redis command*/
          return hashOperations.multiGet(deviceId, fields);
          // Internet research says this exception is thrown when args in an operation are greater than 1024 * 1024 (524287)
      }
}
