package com.pct.device.specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.Predicate;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;

import com.pct.common.constant.DeviceStatus;
import com.pct.common.constant.IOTType;
import com.pct.common.model.Device;
import com.pct.device.util.StringUtils;

public final class SensorSpecification {
	static Logger logger = LoggerFactory.getLogger(SensorSpecification.class);
	
	public static Specification<Device> getSensorListSpecification(String accountNumber, String uuid, DeviceStatus status,String mac,Map<String, String> attributeToValueMap,IOTType type) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList();

            if (!StringUtils.isEmpty(mac)) {
                predicates.add(criteriaBuilder.equal(root.get("macAddress"), mac));
                logger.info("mac "+mac);
            }

            if (!StringUtils.isEmpty(status)) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
                logger.info("Status "+status);
            }
            
            if (!StringUtils.isEmpty(uuid)) {
                predicates.add(criteriaBuilder.equal(root.get("uuid"), uuid));
                logger.info("uuid "+uuid);
            }
            if (!StringUtils.isEmpty(type)) {
                predicates.add(criteriaBuilder.equal(root.get("iotType"), type));
                logger.info("type "+type);
            }

            if (!StringUtils.isEmpty(accountNumber)) {
                predicates.add(criteriaBuilder.equal(root.get("organisation").get("accountNumber"), accountNumber));
                logger.info("accountNumber "+accountNumber);
            }
            attributeToValueMap.forEach((attribute, value) -> {
				if (!StringUtils.isEmpty(value)) {
					if (attribute.equalsIgnoreCase("productName")) {
						predicates.add(criteriaBuilder.like(root.get("productName"), "%" + value + "%"));
						} 
					
					if (attribute.equalsIgnoreCase("productCode")) {
						predicates.add(criteriaBuilder.like(root.get("productCode"), "%" + value + "%"));
						} 				
				}
            });
            return query.where(criteriaBuilder.and(predicates.toArray(new Predicate[0]))).getRestriction();
        };
    }

	public static Specification<Device> getSensorSpec(String can, String uuid,IOTType type, String gatewayId) {
		return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList();
            
            if (!StringUtils.isEmpty(uuid)) {
             predicates.add(criteriaBuilder.equal(root.get("uuid"), uuid));
             logger.info("uuid "+uuid);
         }

         if (!StringUtils.isEmpty(can)) {
             predicates.add(criteriaBuilder.equal(root.get("organisation").get("accountNumber"), can));
             logger.info("accountNumber "+can);
         }
         if (!StringUtils.isEmpty(type)) {
             predicates.add(criteriaBuilder.equal(root.get("iotType"), type));
             logger.info("type "+type);
         }
         if (!StringUtils.isEmpty(gatewayId)) {
             predicates.add(criteriaBuilder.equal(root.get("imei"), gatewayId));
         }
        
         return query.where(criteriaBuilder.and(predicates.toArray(new Predicate[0]))).getRestriction();
		};
	}
	
	public static Specification<Device> getBeaconSpec(String can, String uuid,IOTType type) {
		return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList();
            
            if (!StringUtils.isEmpty(uuid)) {
             predicates.add(criteriaBuilder.equal(root.get("uuid"), uuid));
             logger.info("uuid "+uuid);
         }

         if (!StringUtils.isEmpty(can)) {
             predicates.add(criteriaBuilder.equal(root.get("organisation").get("accountNumber"), can));
             logger.info("accountNumber "+can);
         }
         if (!StringUtils.isEmpty(type)) {
             predicates.add(criteriaBuilder.equal(root.get("iotType"), type));
             logger.info("type "+type);
         }
        
         return query.where(criteriaBuilder.and(predicates.toArray(new Predicate[0]))).getRestriction();
		};
	}
}
