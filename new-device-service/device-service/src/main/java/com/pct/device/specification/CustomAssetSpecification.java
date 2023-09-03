package com.pct.device.specification;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

import javax.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.pct.common.constant.CompanyType;
import com.pct.common.model.Role;
import com.pct.common.model.User;
import com.pct.device.exception.DeviceException;
import com.pct.device.model.AssetRecord;
import com.pct.device.util.AuthoritiesConstants;



public final class CustomAssetSpecification {

	static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static final TimeZone timeZoneUTC = TimeZone.getTimeZone("PST");
	static {
		dateFormat.setTimeZone(timeZoneUTC);
	}
	static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .withZone(ZoneId.of("America/Los_Angeles"));

    private CustomAssetSpecification() {
    }
	
	
	
	public static Specification<AssetRecord> getCampaignSpecification(Map<String, String> attributeToValueMap, User user, String filterModelCountFilter) {
		return (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList();
			List<String> role = user.getRole().stream().map(Role::getRoleName).collect(Collectors.toList());
			if(role.contains(AuthoritiesConstants.INSTALLER) || (role.contains(AuthoritiesConstants.CUSTOMER_ADMIN) && user.getCompany().getType().equals(CompanyType.INSTALLER))) {
				List<Long> companyIds = new ArrayList<>();
				if(user.getCompany() != null && user.getCompany().getAccessList() != null && user.getCompany().getAccessList().size() > 0) {
					for(int i=0; i < user.getCompany().getAccessList().size(); i++) {
						companyIds.add(user.getCompany().getAccessList().get(i).getId());
					}
				}
				predicates.add(root.get("companyId").in(companyIds));
				//customerAssets = assetRecordRepository.getAllAssetRecordsByCompanies(pageable, companyIds);
			}else if (!role.contains(AuthoritiesConstants.SUPER_ADMIN)
					|| (role.contains(AuthoritiesConstants.CUSTOMER_ADMIN) && user.getCompany().getType().equals("Manufacturer"))) {
				predicates.add(criteriaBuilder.equal(root.get("companyId"), user.getCompany().getId()));
			}
			attributeToValueMap.forEach((attribute, value) -> {
				if (!StringUtils.isEmpty(value)) {
					if (attribute.equalsIgnoreCase("createdAt")) {

						Date d2 = null;
						try {
							d2 = dateFormat.parse(value);
						} catch (ParseException e1) {
							e1.printStackTrace();
							throw new DeviceException("createdAt for filter is not in correct format" + value);
						}
						Instant date = Instant.parse(DATE_TIME_FORMATTER.format(d2.toInstant()));
						predicates.add(
								criteriaBuilder.between(root.get("createdAt"), date, date.plus(1, ChronoUnit.DAYS)));
					} 
					if (attribute.equalsIgnoreCase("updatedAt")) {

						Date d2 = null;
						try {
							d2 = dateFormat.parse(value);
						} catch (ParseException e1) {
							e1.printStackTrace();
							throw new DeviceException("createdAt for filter is not in correct format" + value);
						}
						Instant date = Instant.parse(DATE_TIME_FORMATTER.format(d2.toInstant()));
						predicates.add(
								criteriaBuilder.between(root.get("updatedAt"), date, date.plus(1, ChronoUnit.DAYS)));
					} 
					
					if (attribute.equalsIgnoreCase("createdBy")) {
						if (value.equalsIgnoreCase("null")) {
							predicates.add(criteriaBuilder.isTrue(criteriaBuilder.literal(false)));
						}

						else {
							List<Predicate> criteriaStatus = new ArrayList<Predicate>();
							String[] allStatus = value.split(",");
							for (String status : allStatus) {
								String[] userName = status.split(" ");

								String fname = userName[0];
								String lname = userName[1];
								criteriaStatus.add(criteriaBuilder.and(criteriaBuilder.like(root.get("createdFirstName"), fname),
										criteriaBuilder.like(root.get("createdLastName"), lname)));

							}
							predicates.add(criteriaBuilder.or(criteriaStatus.toArray(new Predicate[0])));
						}

					} 
					
					if (attribute.equalsIgnoreCase("updatedBy")) {

						if (value.equalsIgnoreCase("null")) {
							predicates.add(criteriaBuilder.isTrue(criteriaBuilder.literal(false)));
						}

						else {
							List<Predicate> criteriaStatus = new ArrayList<Predicate>();
							String[] allStatus = value.split(",");
							for (String status : allStatus) {
								String[] userName = status.split(" ");
								
								String fname = userName[0];
								String lname = userName[1];
								criteriaStatus.add(criteriaBuilder.and(criteriaBuilder.like(root.get("updatedFirstName"), fname),
										criteriaBuilder.like(root.get("updatedLastName"), lname)));

							}
							predicates.add(criteriaBuilder.or(criteriaStatus.toArray(new Predicate[0])));
						}

					
						
					}
					  
					
					if (attribute.equalsIgnoreCase("companyName")) {
						predicates.add(criteriaBuilder.like(root.get("companyName"), "%" + value + "%"));
						} 
					
					
					if (attribute.equalsIgnoreCase("count")) {
						
						if (filterModelCountFilter.equalsIgnoreCase("equals")) {
							//System.out.println("inside equals");
							predicates.add(criteriaBuilder.equal(root.get("count"), value));
							
						} else if (filterModelCountFilter.equalsIgnoreCase("lessThan")) {
							//System.out.println("inside lessThan");
							predicates.add(criteriaBuilder.lessThan(root.get("count"), value));
							
						} else if (filterModelCountFilter.equalsIgnoreCase("greaterThan")) {
							//System.out.println("inside greaterThan");
							predicates.add(criteriaBuilder.greaterThan(root.get("count"), value));

						}
						//predicates.add(criteriaBuilder.like(root.get("count").as(String.class),  "%" + value + "%"));
						}
						
				}
			});
			return query.where(criteriaBuilder.and(predicates.toArray(new Predicate[0]))).getRestriction();
		};

	}
	
}