package com.pct.device.version.specification;

import java.sql.Timestamp;
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

import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;


import com.pct.device.version.constant.CampaignStatus;
import com.pct.device.version.exception.DeviceVersionException;
import com.pct.device.version.model.CampaignListDisplay;
import com.pct.device.version.model.CampaignStatsPayloadList;
import com.pct.device.version.model.CampaignStep;
import com.pct.device.version.model.Package;
import com.pct.device.version.util.Constants;

import lombok.val;

public final class CampaignSpecification {

	static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static final TimeZone timeZoneUTC = TimeZone.getTimeZone("UTC");
	static {
		dateFormat.setTimeZone(timeZoneUTC);
	}
	static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .withZone(ZoneId.of("America/Los_Angeles"));

    private CampaignSpecification() {
    }

	public static Specification<Package> getPackageSpecification(Map<String, String> attributeToValueMap) {
		return (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList();
			predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));
			attributeToValueMap.forEach((attribute, value) -> {
				if (!StringUtils.isEmpty(value)) {
					if (attribute.equalsIgnoreCase("createdAt")) {

						Date fromDate = null;
						Date toDate = null;
						
						try {
							if(value != null) {
								fromDate = dateFormat.parse(value);
							}
							Instant date = Instant.parse(DATE_TIME_FORMATTER.format(fromDate.toInstant()));
							toDate = dateFormat.parse(value);
							Instant dateFrom = Instant.parse(DATE_TIME_FORMATTER.format(toDate.toInstant()));
							predicates.add(criteriaBuilder.between(root.get("createdAt"), date, dateFrom.plus(1, ChronoUnit.DAYS)));
						} catch (ParseException e1) {
							e1.printStackTrace();
//							throw new DeviceVersionException("createdAt for filter is not in correct format" + value);
						}
						
						
//						if(value != null && value.equalsIgnoreCase("equals")) {
//							predicates.add(criteriaBuilder.between(root.get("createdAt"), date, date.plus(1, ChronoUnit.DAYS)));
//						} else if(value != null && value.equalsIgnoreCase("Before")) {
//							predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), date));
//						} else if(value != null && value.equalsIgnoreCase("After")) {
//							predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), date));
//						} else if(value != null && value.equalsIgnoreCase("inRange")) {
//							try {
//								if(attributeToValueMap.get("dateTo") != null) {
//									toDate = dateFormat.parse(attributeToValueMap.get("dateTo"));
//									Instant dateFrom = Instant.parse(DATE_TIME_FORMATTER.format(toDate.toInstant()));
//									predicates.add(criteriaBuilder.between(root.get("createdAt"), date, dateFrom.plus(1, ChronoUnit.DAYS)));
//								}
//							} catch (ParseException e1) {
//								e1.printStackTrace();
//								throw new DeviceVersionException("createdAt for filter is not in correct format" + value);
//							}
//						}
						
						//predicates.add(criteriaBuilder.between(root.get("createdAt"), date, date.plus(1, ChronoUnit.DAYS)));
						
						
					} else if (attribute.equalsIgnoreCase("deviceType")) {

						List<Predicate> criteriaStatus = new ArrayList<Predicate>();
						String[] allDeviceTppe = value.split(",");
						List<String> list = Arrays.asList(allDeviceTppe);
						In<Object> in = criteriaBuilder.in(root.get("deviceType"));
						in.value(list);
						criteriaStatus.add(in);

						Predicate[] p = new Predicate[criteriaStatus.size()];
						predicates.add(criteriaBuilder.or(criteriaStatus.toArray(p)));

					} else if(!attribute.equalsIgnoreCase("dateFrom") && !attribute.equalsIgnoreCase("dateTo") && !attribute.equalsIgnoreCase("createdAt")) {
						predicates.add(criteriaBuilder.like(root.get(attribute), "%" + value + "%"));
					}
				}
			});
			return query.where(criteriaBuilder.and(predicates.toArray(new Predicate[0]))).getRestriction();
		};

	}
	
	
	public static Specification<CampaignStep> getCampaignSpecification(Map<String, String> attributeToValueMap) {
		return (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList();
			predicates.add(criteriaBuilder.equal(root.get("campaign").get("isDeleted"), false));
			attributeToValueMap.forEach((attribute, value) -> {
				if (!StringUtils.isEmpty(value)) {
					if (attribute.equalsIgnoreCase("createdAt")) {

						Date d2 = null;
						try {
							d2 = dateFormat.parse(value);
						} catch (ParseException e1) {
							e1.printStackTrace();
							throw new DeviceVersionException("createdAt for filter is not in correct format" + value);
						}
						Instant date = Instant.parse(DATE_TIME_FORMATTER.format(d2.toInstant()));
						predicates.add(
								criteriaBuilder.between(root.get("campaign").get("createdAt"), date, date.plus(1, ChronoUnit.DAYS)));
					} 
					
					if (attribute.equalsIgnoreCase("createdBy")) {
						predicates.add(criteriaBuilder.like(root.get("campaign").get("createdBy").get("firstName"), "%" + value + "%"));
						predicates.add(criteriaBuilder.like(root.get("campaign").get("createdBy").get("lastName"), "%" + value + "%"));
						} 
					
					if (attribute.equalsIgnoreCase("campaignName")) {
						predicates.add(criteriaBuilder.like(root.get("campaign").get("campaignName"), "%" + value + "%"));
						} 
					
					if (attribute.equalsIgnoreCase("firstStep")) {
						predicates.add(criteriaBuilder.like(root.get("fromPackage").get("packageName"), "%" + value + "%"));
						} 

					if (attribute.equalsIgnoreCase("firstStep")) {
						predicates.add(criteriaBuilder.equal(root.get("stepOrderNumber"), 1l ));
						} 
						
					if (attribute.equalsIgnoreCase("lastStep")) {
						predicates.add(criteriaBuilder.like(root.get("toPackage").get("packageName"), "%" + value + "%"));
						}
					if (attribute.equalsIgnoreCase("imeiGroup")) {
						predicates.add(criteriaBuilder.like(root.get("campaign").get("group").get("groupingName"), "%" + value + "%"));
						}
					if (attribute.equalsIgnoreCase("campaignStatus")) {

						List<Predicate> criteriaStatus = new ArrayList<Predicate>();
						String[] allStatus = value.split(Constants.COMMA);
						for (String status : allStatus) {
							CampaignStatus resultSearch = CampaignStatus.getCampaignStatusInSearch(status);
							criteriaStatus.add(
									criteriaBuilder.equal(root.get("campaign").get("campaignStatus"), resultSearch));
						}
						predicates.add(criteriaBuilder.or(criteriaStatus.toArray(new Predicate[0])));
					}
				}
			});
			return query.where(criteriaBuilder.and(predicates.toArray(new Predicate[0]))).getRestriction();
		};

	}

	public static Specification<CampaignListDisplay> getCSPLSpecification(Map<String, String> attributeToValueMap) {
		return (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList();
			attributeToValueMap.forEach((attribute, value) -> {
				if (!StringUtils.isEmpty(value)) {
					if (attribute.equalsIgnoreCase("campaignStartDate")) {

						Date d2 = null;
						try {
							d2 = dateFormat.parse(value);
						} catch (ParseException e1) {
							e1.printStackTrace();
							throw new DeviceVersionException("campaignStartDate for filter is not in correct format" + value);
						}
						Instant date = Instant.parse(DATE_TIME_FORMATTER.format(d2.toInstant()));
						predicates.add(
								criteriaBuilder.between(root.get("campaignStartDate"), date, date.plus(1, ChronoUnit.DAYS)));
					} else if (attribute.equalsIgnoreCase("deviceType")) {

						List<Predicate> criteriaStatus = new ArrayList<Predicate>();
						String[] allDeviceTppe = value.split(",");
						List<String> list = Arrays.asList(allDeviceTppe);
						In<Object> in = criteriaBuilder.in(root.get("deviceType"));
						in.value(list);
						criteriaStatus.add(in);

						Predicate[] p = new Predicate[criteriaStatus.size()];
						predicates.add(criteriaBuilder.or(criteriaStatus.toArray(p)));

					} else {
						if (attribute.equalsIgnoreCase("problemCount")) {
							predicates.add(criteriaBuilder.equal(root.get("problemCount"), value));
						} else {

							if (attribute.equalsIgnoreCase("completed")) {
								predicates.add(criteriaBuilder.equal(root.get("completed"), value));
							} else {

								if (attribute.equalsIgnoreCase("eligible")) {
									predicates.add(criteriaBuilder.equal(root.get("eligible"), value));
								} else {

									if (attribute.equalsIgnoreCase("inProgress")) {
										predicates.add(criteriaBuilder.equal(root.get("inProgress"), value));
									} else {
										if (attribute.equalsIgnoreCase("notStarted")) {
											predicates.add(criteriaBuilder.equal(root.get("notStarted"), value));
										} else {
											predicates
													.add(criteriaBuilder.like(root.get(attribute), "%" + value + "%"));
										}
									}
								}
							}
						}
					}
				}
			});
			return query.where(criteriaBuilder.and(predicates.toArray(new Predicate[0]))).getRestriction();
		};

	}
	
}