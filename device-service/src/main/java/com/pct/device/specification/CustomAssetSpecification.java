package com.pct.device.specification;

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

import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

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

	public static Specification<AssetRecord> getCustomerAsetSpecification(Map<String, String> attributeToValueMap,
			User user, String filterModelCountFilter, String sort) {
		return (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();

			boolean roleAvailable2 = false;
				for (Role roles : user.getRole()) {
//					|| user.getOrganisation().getType().equals("Manufacturer")
					if ((!roles.getName().contains(AuthoritiesConstants.SUPER_ADMIN)&&!roles.getName().contains(AuthoritiesConstants.ROLE_PCT_CONNECT))
							|| (roles.getName().contains(AuthoritiesConstants.ROLE_CUSTOMER_ADMIN)
									|| roles.getName().contains(AuthoritiesConstants.ROLE_ORGANIZATION_USER)
									|| roles.getName().contains(AuthoritiesConstants.ROLE_CALL_CENTER))) {
						roleAvailable2 = true;
						break;
					}
			}
			if (roleAvailable2) {
				predicates.add(criteriaBuilder.equal(root.get("organisationId"), user.getOrganisation().getId()));
			}
			
			if(sort != null) {
				if(sort.contains(".")) {
					Path path = null;
					From from = root;
					String [] sortArr = sort.split("[.]");
					for(int i = 0; i < sortArr.length; i++) {
						if(i == sortArr.length-1) {
							path = from.get(sortArr[i]);
						} else {
							from = from.join(sortArr[i]);
						}
					}
					predicates.add(criteriaBuilder.and(criteriaBuilder.isNotNull(path), criteriaBuilder.notEqual(path.as(String.class), "")));
				} else {
					predicates.add(criteriaBuilder.and(criteriaBuilder.isNotNull(root.get(sort)), criteriaBuilder.notEqual(root.get(sort).as(String.class), "")));
				}
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
						predicates.add(criteriaBuilder.between(root.get("createdAt"), date,
								date.plus(1439, ChronoUnit.MINUTES)));
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
						predicates.add(criteriaBuilder.between(root.get("updatedAt"), date,
								date.plus(1439, ChronoUnit.MINUTES)));
					}

					if (attribute.equalsIgnoreCase("createdBy")) {
//						predicates.add(criteriaBuilder.and(criteriaBuilder.like(root.get("createdFirstName"), value),
//								criteriaBuilder.like(root.get("createdLastName"), value)));
						predicates.add(criteriaBuilder.like(root.get("createdFirstName"), "%" + value + "%"));
//						if (value.equalsIgnoreCase("null")) {
//							predicates.add(criteriaBuilder.isTrue(criteriaBuilder.literal(false)));
//						}
//
//						else {
//							List<Predicate> criteriaStatus = new ArrayList<Predicate>();
//							String[] allStatus = value.split(",");
//							for (String status : allStatus) {
//								String[] userName = status.split(" ");
//
//								String fname = userName[0];
//								String lname = "";
//								if (userName.length > 0) {
//									lname = userName[1];
//								}
//								criteriaStatus.add(
//										criteriaBuilder.and(criteriaBuilder.like(root.get("createdFirstName"), fname),
//												criteriaBuilder.like(root.get("createdLastName"), lname)));
//
//							}
//							predicates.add(criteriaBuilder.or(criteriaStatus.toArray(new Predicate[0])));
//						}

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
								criteriaStatus.add(
										criteriaBuilder.and(criteriaBuilder.like(root.get("updatedFirstName"), fname),
												criteriaBuilder.like(root.get("updatedLastName"), lname)));

							}
							predicates.add(criteriaBuilder.or(criteriaStatus.toArray(new Predicate[0])));
						}
					}

					if (attribute.equalsIgnoreCase("organisationName")) {
						predicates.add(criteriaBuilder.like(root.get("organisationName"), "%" + value + "%"));
					}
					
					if (attribute.equalsIgnoreCase("forwardingGroup")) {
						if (value.equalsIgnoreCase("null")) {
							predicates.add(criteriaBuilder.isNull(root.get("forwardingGroup")));
						} else {
							String[] forwardingGroupArray = value.split(",");
							List<String> forwardingGroupList = Arrays.asList(forwardingGroupArray);
							predicates.add(criteriaBuilder.in(root.get("forwardingGroup")).value(forwardingGroupList));
						}
					}
					
					if (attribute.equalsIgnoreCase("parentGroup")) {
						if (value.equalsIgnoreCase("null")) {
							predicates.add(criteriaBuilder.isNull(root.get("parentGroup")));
						} else {
							String[] purchaseByArray = value.split(",");
							List<String> purchaseByList = Arrays.asList(purchaseByArray);
							List<Predicate> purchaseByPredicates = new ArrayList<>();
							for (String purchaseByName : purchaseByList) {
								if(purchaseByName != null && !purchaseByName.isEmpty()) {
									purchaseByPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("parentGroup")), "%" + purchaseByName.toLowerCase() + "%"));
								}
							}
							if(!purchaseByPredicates.isEmpty()) {
								predicates.add(criteriaBuilder.or(purchaseByPredicates.toArray(new Predicate[0])));
							}
						}
					}

					if (attribute.equalsIgnoreCase("count")) {

						if (filterModelCountFilter.equalsIgnoreCase("equals")) {
							// System.out.println("inside equals");
							predicates.add(criteriaBuilder.equal(root.get("count"), value));

						} else if (filterModelCountFilter.equalsIgnoreCase("lessThan")) {
							// System.out.println("inside lessThan");
							predicates.add(criteriaBuilder.lessThan(root.get("count"), value));

						} else if (filterModelCountFilter.equalsIgnoreCase("greaterThan")) {
							// System.out.println("inside greaterThan");
							predicates.add(criteriaBuilder.greaterThan(root.get("count"), value));

						}
						// predicates.add(criteriaBuilder.like(root.get("count").as(String.class), "%" +
						// value + "%"));
					}

				}
			});
			return query.where(criteriaBuilder.and(predicates.toArray(new Predicate[0]))).getRestriction();
		};

	}

}