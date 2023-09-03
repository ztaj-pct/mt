package com.pct.device.specification;

import java.time.Instant;
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
import com.pct.common.model.Role;
import com.pct.common.model.User;
import com.pct.device.model.GatewaySummary;
import com.pct.device.util.AuthoritiesConstants;
import com.pct.device.util.StringUtils;

public final class GatewaySpecification {
	static Logger logger = LoggerFactory.getLogger(GatewaySpecification.class);

	public static Specification<Device> getGatewayListSpecification(String accountNumber, String imei, String uuid,
			DeviceStatus status, Map<String, String> attributeToValueMap, IOTType type, String mac, Instant lastDownloadeTime) {
		return (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (!StringUtils.isEmpty(imei)) {
				predicates.add(criteriaBuilder.equal(root.get("imei"), imei));
				logger.info("IMEIIIIII " + imei);
			}

			if (!StringUtils.isEmpty(status)) {
				predicates.add(criteriaBuilder.equal(root.get("status"), status));
				logger.info("Status " + status);
			}
			if (!StringUtils.isEmpty(type)) {
				predicates.add(criteriaBuilder.equal(root.get("iotType"), type));
				logger.info("type " + type);
			}

			if (!StringUtils.isEmpty(uuid)) {
				predicates.add(criteriaBuilder.equal(root.get("uuid"), uuid));
				logger.info("uuid " + uuid);
			}

			if (!StringUtils.isEmpty(accountNumber)) {
				predicates.add(criteriaBuilder.equal(root.get("organisation").get("accountNumber"), accountNumber));
				logger.info("accountNumber " + accountNumber);
			}
			attributeToValueMap.forEach((attribute, value) -> {
				if (!StringUtils.isEmpty(value)) {
					if (attribute.equalsIgnoreCase("productName")) {
						predicates.add(criteriaBuilder.like(root.get("productName"), "%" + value + "%"));
					}

					if (attribute.equalsIgnoreCase("productCode")) {
						predicates.add(criteriaBuilder.like(root.get("productCode"), "%" + value + "%"));
					}

					if (attribute.equalsIgnoreCase("imei")) {
						predicates.add(criteriaBuilder.like(root.get("imei"), "%" + value + "%"));
					}
				}
			});
			
			  if(lastDownloadeTime != null) {
	            	 predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("timeOfLastDownload"), lastDownloadeTime));
	            }
			return query.where(criteriaBuilder.and(predicates.toArray(new Predicate[0]))).getRestriction();
		};
	}

	public static Specification<Device> getGatewaySpec(String can, String imei, String uuid, IOTType type) {
		return (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList();

			if (!StringUtils.isEmpty(uuid)) {
				predicates.add(criteriaBuilder.equal(root.get("uuid"), uuid));
				logger.info("uuid " + uuid);
			}

			if (!StringUtils.isEmpty(can)) {
				predicates.add(criteriaBuilder.equal(root.get("organisation").get("accountNumber"), can));
				logger.info("accountNumber " + can);
			}
			if (!StringUtils.isEmpty(imei)) {
				predicates.add(criteriaBuilder.equal(root.get("imei"), imei));
				logger.info("imei " + imei);
			}
			if (!StringUtils.isEmpty(type)) {
				predicates.add(criteriaBuilder.equal(root.get("iotType"), type));
				logger.info("type " + type);
			}
			return query.where(criteriaBuilder.and(predicates.toArray(new Predicate[0]))).getRestriction();
		};
	}

	public static Specification<Device> getGatewayspec(String can, String uuid, IOTType type) {
		return (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList();

			if (!StringUtils.isEmpty(uuid)) {
				predicates.add(criteriaBuilder.equal(root.get("uuid"), uuid));
				logger.info("uuid " + uuid);
			}

			if (!StringUtils.isEmpty(can)) {
				predicates.add(criteriaBuilder.equal(root.get("organisation").get("accountNumber"), can));
				logger.info("accountNumber " + can);
			}
			if (!StringUtils.isEmpty(type)) {
				predicates.add(criteriaBuilder.equal(root.get("iotType"), type));
				logger.info("type " + type);
			}

			return query.where(criteriaBuilder.and(predicates.toArray(new Predicate[0]))).getRestriction();
		};
	}

	public static Specification<Device> getGatewaySensorSpec(String imei, String uuid, IOTType type) {
		return (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList();

			if (!StringUtils.isEmpty(uuid)) {
				predicates.add(criteriaBuilder.equal(root.get("uuid"), uuid));
				logger.info("uuid " + uuid);
			}
			if (!StringUtils.isEmpty(imei)) {
				predicates.add(criteriaBuilder.equal(root.get("imei"), imei));
				logger.info("imei " + imei);
			}
			if (!StringUtils.isEmpty(type)) {
				predicates.add(criteriaBuilder.equal(root.get("iotType"), type));
				logger.info("type " + type);
			}
			return query.where(criteriaBuilder.and(predicates.toArray(new Predicate[0]))).getRestriction();
		};
	}

	public static Specification<GatewaySummary> getSpecificationForGatewaySummary(
			Map<String, String> attributeToValueMap, User user) {
		return (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();

			boolean roleAvailable = false;
			for (Role roles : user.getRole()) {
				if (roles.getName().equalsIgnoreCase(AuthoritiesConstants.SUPER_ADMIN)
						|| roles.getName().equalsIgnoreCase(AuthoritiesConstants.ROLE_CUSTOMER_ADMIN)) {
					roleAvailable = true;
					break;
				}
			}
//			if (!(roleAvailable && user.getOrganisation().getType().getValue().equals("Manufacturer"))) {
//				predicates.add(criteriaBuilder.equal(root.get("organisationId"), user.getOrganisation().getId()));
//			}

			attributeToValueMap.forEach((attribute, value) -> {
				if (!StringUtils.isEmpty(value)) {
					if (attribute.equalsIgnoreCase("organisationName")) {
						predicates.add(criteriaBuilder.like(root.get("organisationName"), "%" + value + "%"));
					} else if (attribute.equalsIgnoreCase("smartSeven")) {
						predicates.add(criteriaBuilder.equal(root.get("smartSeven"), Long.parseLong(value)));
					} else if (attribute.equalsIgnoreCase("traillerNet")) {
						predicates.add(criteriaBuilder.equal(root.get("traillerNet"), Long.parseLong(value)));
					} else if (attribute.equalsIgnoreCase("smartPair")) {
						predicates.add(criteriaBuilder.equal(root.get("smartPair"), Long.parseLong(value)));
					} else if (attribute.equalsIgnoreCase("stealthNet")) {
						predicates.add(criteriaBuilder.equal(root.get("stealthNet"), Long.parseLong(value)));
					} else if (attribute.equalsIgnoreCase("sabre")) {
						predicates.add(criteriaBuilder.equal(root.get("sabre"), Long.parseLong(value)));
					} else if (attribute.equalsIgnoreCase("freightLa")) {
						predicates.add(criteriaBuilder.equal(root.get("freightLa"), Long.parseLong(value)));
					} else if (attribute.equalsIgnoreCase("arrowL")) {
						predicates.add(criteriaBuilder.equal(root.get("arrowL"), Long.parseLong(value)));
					} else if (attribute.equalsIgnoreCase("freightL")) {
						predicates.add(criteriaBuilder.equal(root.get("freightL"), Long.parseLong(value)));
					} else if (attribute.equalsIgnoreCase("cutlassL")) {
						predicates.add(criteriaBuilder.equal(root.get("cutlassL"), Long.parseLong(value)));
					} else if (attribute.equalsIgnoreCase("dagger67Lg")) {
						predicates.add(criteriaBuilder.equal(root.get("dagger67Lg"), Long.parseLong(value)));
					} else if (attribute.equalsIgnoreCase("smart7")) {
						predicates.add(criteriaBuilder.equal(root.get("smart7"), Long.parseLong(value)));
					} else if (attribute.equalsIgnoreCase("katanaH")) {
						predicates.add(criteriaBuilder.equal(root.get("katanaH"), Long.parseLong(value)));
					} else if (attribute.equalsIgnoreCase("count")) {
						predicates.add(criteriaBuilder.equal(root.get("count"), Long.parseLong(value)));
					}
				}
			});
			return query.where(criteriaBuilder.and(predicates.toArray(new Predicate[0]))).getRestriction();
		};

	}
	
	
}
