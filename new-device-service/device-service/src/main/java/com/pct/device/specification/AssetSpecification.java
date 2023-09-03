package com.pct.device.specification;

import com.pct.common.constant.AssetCategory;
import com.pct.common.constant.AssetStatus;
import com.pct.common.model.Asset;
import com.pct.common.model.Role;
import com.pct.common.model.User;
import com.pct.common.constant.AssetCategory;
import com.pct.common.constant.AssetStatus;
import com.pct.device.exception.DeviceException;
import com.pct.device.model.AssetRecord;
import com.pct.device.util.AuthoritiesConstants;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.CriteriaBuilder.In;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;



public final class AssetSpecification {

    private AssetSpecification() {
    }

    public static Specification<Asset> getAssetListSpecification(String accountNumber, String vin, String assignedName, String status, String eligibleGateway) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList();

            if (!StringUtils.isEmpty(accountNumber)) {
                predicates.add(criteriaBuilder.equal(root.get("company").get("accountNumber"), accountNumber));
            }

            if (!StringUtils.isEmpty(vin)) {
                predicates.add(criteriaBuilder.equal(root.get("vin"), vin));
            }

            if (!StringUtils.isEmpty(assignedName)) {
                predicates.add(criteriaBuilder.equal(root.get("assignedName"), assignedName));
            }

            if (!StringUtils.isEmpty(status)) {
                predicates.add(criteriaBuilder.equal(root.get("status"), AssetStatus.getAssetStatus(status)));
            }

            if (!StringUtils.isEmpty(eligibleGateway)) {
                predicates.add(criteriaBuilder.equal(root.get("gatewayEligibility"), eligibleGateway));
            }
            
            	

            return query.where(criteriaBuilder.and(predicates.toArray(new Predicate[0]))).getRestriction();
        };
    }
    

	static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static final TimeZone timeZoneUTC = TimeZone.getTimeZone("PST");
	static {
		dateFormat.setTimeZone(timeZoneUTC);
	}
	static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .withZone(ZoneId.of("America/Los_Angeles"));

	
	
	
	public static Specification<Asset> getaAssetSpecification(Map<String, String> attributeToValueMap, User user,Long companyId,String yearFilter) {
		return (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList();
			List<String> role = user.getRole().stream().map(Role::getRoleName).collect(Collectors.toList());
			if ((role.contains(AuthoritiesConstants.SUPER_ADMIN) || role.contains(AuthoritiesConstants.CUSTOMER_ADMIN) || role.contains(AuthoritiesConstants.INSTALLER)) && companyId != null) {
				predicates.add(criteriaBuilder.equal(root.get("company").get("id"), companyId));
			}else {
				predicates.add(criteriaBuilder.equal(root.get("company").get("id"), user.getCompany().getId()));
			}
			attributeToValueMap.forEach((attribute, value) -> {
				if (!StringUtils.isEmpty(value)) {
					if (attribute.equalsIgnoreCase("datetimeCreated")) {

						Date d2 = null;
						try {
							d2 = dateFormat.parse(value);
						} catch (ParseException e1) {
							e1.printStackTrace();
							throw new DeviceException("createdAt for filter is not in correct format" + value);
						}
						Instant date = Instant.parse(DATE_TIME_FORMATTER.format(d2.toInstant()));
						predicates.add(
								criteriaBuilder.between(root.get("createdOn"), date, date.plus(1, ChronoUnit.DAYS)));
					} 
					if (attribute.equalsIgnoreCase("datetimeUpdated")) {

						Date d2 = null;
						try {
							d2 = dateFormat.parse(value);
						} catch (ParseException e1) {
							e1.printStackTrace();
							throw new DeviceException("createdAt for filter is not in correct format" + value);
						}
						Instant date = Instant.parse(DATE_TIME_FORMATTER.format(d2.toInstant()));
						predicates.add(
								criteriaBuilder.between(root.get("updatedOn"), date, date.plus(1, ChronoUnit.DAYS)));
					} 
					
					if (attribute.equalsIgnoreCase("assignedName")) {
						predicates.add(criteriaBuilder.like(root.get("assignedName"), "%" + value + "%"));
						
						} 
					if (attribute.equalsIgnoreCase("category")) {
						System.out.println("value: "+ value);
						if (value.equalsIgnoreCase("null")) {
							predicates.add(criteriaBuilder.isTrue(criteriaBuilder.literal(false)));
						}

						
						List<Predicate> criteriaStatus = new ArrayList<Predicate>();
						String[] allStatus = value.split(",");
						
						for (String status : allStatus) {
							System.out.println(status);
							
							if (status.equalsIgnoreCase("Trailer"))
								
								criteriaStatus.add(criteriaBuilder.equal(root.get("category"), AssetCategory.TRAILER));

							if (status.equalsIgnoreCase("Chassis"))
								
								criteriaStatus.add(criteriaBuilder.equal(root.get("category"), AssetCategory.CHASSIS));

							if (status.equalsIgnoreCase("Container"))
								
								criteriaStatus.add(criteriaBuilder.equal(root.get("category"), AssetCategory.CONTAINER));
							if (status.equalsIgnoreCase("Vehicle"))
								
								criteriaStatus.add(criteriaBuilder.equal(root.get("category"), AssetCategory.VEHICLE));
							if (status.equalsIgnoreCase("Tractor"))
								
								criteriaStatus.add(criteriaBuilder.equal(root.get("category"), AssetCategory.TRACTOR));
						}
						predicates.add(criteriaBuilder.or(criteriaStatus.toArray(new Predicate[0])));

					} 
					
					
					
					if (attribute.equalsIgnoreCase("status")) {
//						if(value.equalsIgnoreCase("null")) 
//							predicates.add(criteriaBuilder.isTrue(criteriaBuilder.literal(false)));
						List<Predicate> criteriaStatus = new ArrayList<Predicate>();
						String[] allStatus = value.split(",");
						for (String status : allStatus) {
							if (status.equalsIgnoreCase(AssetStatus.ACTIVE.toString()))
								criteriaStatus.add(criteriaBuilder.equal(root.get("status"), AssetStatus.ACTIVE));
							else if (status.equalsIgnoreCase(AssetStatus.INACTIVE.toString()))
								criteriaStatus.add(criteriaBuilder.equal(root.get("status"), AssetStatus.INACTIVE));
							else if (status.equalsIgnoreCase(AssetStatus.DELETED.toString()))
								criteriaStatus.add(criteriaBuilder.equal(root.get("status"), AssetStatus.DELETED));
							else if (status.equalsIgnoreCase(AssetStatus.ERROR.toString()))
								criteriaStatus.add(criteriaBuilder.equal(root.get("status"), AssetStatus.ERROR));
							else if (status.equalsIgnoreCase("Install In Progress"))
								criteriaStatus.add(criteriaBuilder.equal(root.get("status"), AssetStatus.INSTALL_IN_PROGRESS));
							else if (status.equalsIgnoreCase(AssetStatus.PARTIAL.toString()))
								criteriaStatus.add(criteriaBuilder.equal(root.get("status"), AssetStatus.PARTIAL));
							else if (status.equalsIgnoreCase(AssetStatus.PENDING.toString()))
								criteriaStatus.add(criteriaBuilder.equal(root.get("status"), AssetStatus.PENDING));
						}
						predicates.add(criteriaBuilder.or(criteriaStatus.toArray(new Predicate[0])));
					}
						 
					if (attribute.equalsIgnoreCase("eligible_gateway")) {
						System.out.println("inside eligible_gateway");
						
						
						List<Predicate> criteriaStatus = new ArrayList<Predicate>();
						String[] allStatus = value.split(",");
						List<String> list = Arrays.asList(allStatus);
						In<Object> in = criteriaBuilder.in(root.get("gatewayEligibility"));
						in.value(list) ;
						criteriaStatus.add(in) ;
						
						Predicate[] p = new Predicate[criteriaStatus.size()];  
						predicates.add(criteriaBuilder.or(criteriaStatus.toArray(p)));
						
						
						} 
					
					if (attribute.equalsIgnoreCase("vin")) {
						predicates.add(criteriaBuilder.like(root.get("vin"), "%" + value + "%"));
						}
					if (attribute.equalsIgnoreCase("comment")) {
						predicates.add(criteriaBuilder.like(root.get("comment"), "%" + value + "%"));
						}
					
					if (attribute.equalsIgnoreCase("year")) {
						
						System.out.println(value.length());
						for(int i=0; i<(4-value.length()); i++) {value ="0"+value;}

						if (yearFilter.equalsIgnoreCase("equals")) {
							//System.out.println("inside equals");
							predicates.add(criteriaBuilder.equal(root.get("year"), value));
							
						} else if (yearFilter.equalsIgnoreCase("lessThan")) {
							//System.out.println("inside lessThan");
							predicates.add(criteriaBuilder.lessThan(root.get("year"), value));
							//predicates.add(criteriaBuilder.lt(root.get("year"), Integer.parseInt(value)));
							
						} else if (yearFilter.equalsIgnoreCase("lessThanOrEqual")) {
							//System.out.println("inside lessThanOrEqual");
							predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("year"), value));
							//predicates.add(criteriaBuilder.le(root.get("year"), Integer.parseInt(value)));

						} else if (yearFilter.equalsIgnoreCase("greaterThan")) {
							//System.out.println("inside greaterThan");
							predicates.add(criteriaBuilder.greaterThan(root.get("year"), value));
							//predicates.add(criteriaBuilder.gt(root.get("year"), Integer.parseInt(value)));

						} else if (yearFilter.equalsIgnoreCase("greaterThanOrEqual")) {
							//System.out.println("inside greaterThanOrEqual");
							predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("year"), value));
							//predicates.add(criteriaBuilder.ge(root.get("year"), Integer.parseInt(value)));

						}
						//predicates.add(criteriaBuilder.like(root.get("year"), "%" + value + "%"));
					} 
					if (attribute.equalsIgnoreCase("make")) {
						// predicates.add(criteriaBuilder.like(root.get("manufacturer").get("name"), "%"
						// + value + "%"));
						List<Predicate> criteriaStatus = new ArrayList<Predicate>();
						String[] allStatus = value.split(",");
						for (String status : allStatus) {
							criteriaStatus.add(criteriaBuilder.equal(root.get("manufacturer").get("name"), status));
						}
						predicates.add(criteriaBuilder.or(criteriaStatus.toArray(new Predicate[0])));
					} 
					
				}
			});
			return query.where(criteriaBuilder.and(predicates.toArray(new Predicate[0]))).getRestriction();
		};

	}

}