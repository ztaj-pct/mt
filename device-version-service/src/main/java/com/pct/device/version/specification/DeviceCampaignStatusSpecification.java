package com.pct.device.version.specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.pct.device.version.model.DeviceCampaignStatus;

@Component
public class DeviceCampaignStatusSpecification {
	public Specification<DeviceCampaignStatus> getSpecification(String campaignUUID,
			Map<String, String> attributeToValueMap) {
		return (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();
			predicates.add(criteriaBuilder.equal(root.get("campaign").get("uuid"), campaignUUID));

			attributeToValueMap.forEach((attribute, value) -> {
				if (StringUtils.hasText(value)) {
					if (attribute.equalsIgnoreCase("imei")) {
						predicates.add(criteriaBuilder.like(root.get("deviceId"), "%" + value + "%"));
					}

					if (attribute.equalsIgnoreCase("customerName")) {
						
 							List<Predicate> criteriaCustomer = new ArrayList<Predicate>();
							String[] allCustomer = value.split(",");
							for (String customer : allCustomer) {
								criteriaCustomer
										.add(criteriaBuilder.equal(root.get("customerName"), customer));
							}
							predicates.add(criteriaBuilder.or(criteriaCustomer.toArray(new Predicate[0])));
  					//	predicates.add(criteriaBuilder.like(root.get("customerName"), "%" + value + "%"));
					}

					if (attribute.equalsIgnoreCase("deviceStatusForCampaign")) {
						List<Predicate> criteriaStatus = new ArrayList<Predicate>();
						String[] allStatus = value.split(",");
						for (String pStatus : allStatus) {
							criteriaStatus
									.add(criteriaBuilder.equal(root.get("runningStatus"), pStatus));
						}
						predicates.add(criteriaBuilder.or(criteriaStatus.toArray(new Predicate[0])));
						
						
						//predicates.add(criteriaBuilder.like(root.get("runningStatus"), "%" + value + "%"));
					}

					if (attribute.equalsIgnoreCase("lastReport")) {
						predicates.add(criteriaBuilder.like(root.<Instant>get("lastReportedAt").as(String.class),
								"%" + value + "%"));
					}

					if (attribute.equalsIgnoreCase("device_status_for_campaign")) {
						predicates.add(criteriaBuilder.like(root.get("campaignStatus"), "%" + value + "%"));
					}

//					if (attribute.equalsIgnoreCase("device_status_for_campaign")) {
//						predicates.add(criteriaBuilder.like(root.get("campaignStatus"), "%" + value + "%"));
//					}

					// TODO Installed_Flag flag is the field of CampaignInstalledDevice which is
					// part of connectedtacking database add filtering for this at the end

//					if (attribute.equalsIgnoreCase("Installed_Flag")) {
//						predicates.add(criteriaBuilder.like(root.get("Installed_Flag"), value));
//					}
				}
			});
			return query.where(criteriaBuilder.and(predicates.toArray(new Predicate[0]))).getRestriction();
		};
	}
}