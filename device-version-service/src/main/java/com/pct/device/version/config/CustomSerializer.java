package com.pct.device.version.config;

import java.util.Map;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pct.device.version.payload.ExecuteCampaignRequest;


public class CustomSerializer implements Serializer<ExecuteCampaignRequest> {

	@Override
	public byte[] serialize(String topic, ExecuteCampaignRequest data) 
	{
		final ObjectMapper objectMapper = new ObjectMapper();
		
		try 
		{
			if (data == null) {
				System.out.println("Null received at serializing");
				return null;
			}
			
			System.out.println("Serializing...");
			return objectMapper.writeValueAsBytes(data);
		}
		catch (Exception e) {
			throw new SerializationException("Error when serializing MessageDto to byte[]");
		}
	}

	@Override
	public void configure(Map<String, ?> configs, boolean isKey) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}
	
}
