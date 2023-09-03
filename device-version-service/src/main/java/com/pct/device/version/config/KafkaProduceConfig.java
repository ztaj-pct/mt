package com.pct.device.version.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import com.pct.device.version.payload.ExecuteCampaignRequest;



@Configuration
public class KafkaProduceConfig {
	
	//this method is used to kafka producer config details
	@Bean
	public ProducerFactory<String, ExecuteCampaignRequest> producerFactory(){
		Map<String, Object> configProps = new HashMap<>();
		configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"b-2.prodms1ms2bridgekafka.rt5sk3.c8.kafka.us-east-1.amazonaws.com:9092,b-1.prodms1ms2bridgekafka.rt5sk3.c8.kafka.us-east-1.amazonaws.com:9092,b-3.prodms1ms2bridgekafka.rt5sk3.c8.kafka.us-east-1.amazonaws.com:9092");
		configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,StringSerializer.class);
		configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,"com.pct.device.version.config.CustomSerializer");
		configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
		return new DefaultKafkaProducerFactory<>(configProps);
	}
	
	//this method is used to create kafkatemplate bean obj
	@Bean
	public 	KafkaTemplate<String,ExecuteCampaignRequest> kafkaTemplate(){
		return new KafkaTemplate<String,ExecuteCampaignRequest>(producerFactory());
	}
	
}
