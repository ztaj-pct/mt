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

import com.pct.common.model.MaintenanceReportHistory;
import com.pct.common.model.Role;
import com.pct.common.model.User;
import com.pct.device.exception.DeviceException;
import com.pct.device.model.AssetRecord;
import com.pct.device.util.AuthoritiesConstants;

public final class MaintenanceReportHistorySpecification {

	static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static final TimeZone timeZoneUTC = TimeZone.getTimeZone("PST");
	static {
		dateFormat.setTimeZone(timeZoneUTC);
	}
	static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
			.withZone(ZoneId.of("America/Los_Angeles"));

	public static Specification<MaintenanceReportHistory> getMaintenanceReportHistorySpecification(
			Map<String, String> attributeToValueMap, User user, String filterModelCountFilter, String organistionName,
			Integer days, String sort, boolean isUuid) {
		return (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (!isUuid) {
				boolean roleAvailable2 = false;
				for (Role roles : user.getRole()) {
					if ((!roles.getName().contains(AuthoritiesConstants.SUPER_ADMIN)
							&& !roles.getName().contains(AuthoritiesConstants.ROLE_PCT_CONNECT))
							|| (roles.getName().contains(AuthoritiesConstants.ROLE_CUSTOMER_ADMIN)
									|| roles.getName().contains(AuthoritiesConstants.ROLE_ORGANIZATION_USER)
									|| roles.getName().contains(AuthoritiesConstants.ROLE_CALL_CENTER))) {
						roleAvailable2 = true;
						break;
					}
				}

				if (roleAvailable2) {
					predicates.add(
							criteriaBuilder.equal(root.get("organisation").get("id"), user.getOrganisation().getId()));
				}

				if (!StringUtils.isEmpty(organistionName) && !organistionName.equalsIgnoreCase("0")) {
					predicates.add(
							criteriaBuilder.equal(root.get("organisation").get("organisationName"), organistionName));
				}

				if (days != null && days > 0) {
					Date d = new Date();
					Instant date = Instant.parse(DATE_TIME_FORMATTER.format(d.toInstant()));
					predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdDate"),
							date.minus(days, ChronoUnit.DAYS)));
				}
			}
			if (sort != null) {
				if (sort.contains(".")) {
					Path path = null;
					From from = root;
					String[] sortArr = sort.split("[.]");
					for (int i = 0; i < sortArr.length; i++) {
						if (i == sortArr.length - 1) {
							path = from.get(sortArr[i]);
						} else {
							from = from.join(sortArr[i]);
						}
					}
					predicates.add(criteriaBuilder.and(criteriaBuilder.isNotNull(path),
							criteriaBuilder.notEqual(path.as(String.class), "")));
				} else {
					predicates.add(criteriaBuilder.and(criteriaBuilder.isNotNull(root.get(sort)),
							criteriaBuilder.notEqual(root.get(sort).as(String.class), "")));
				}
			}

			attributeToValueMap.forEach((attribute, value) -> {
				if (!StringUtils.isEmpty(value)) {
					if (attribute.equalsIgnoreCase("createdDate")) {

						Date d2 = null;
						try {
							d2 = dateFormat.parse(value);
						} catch (ParseException e1) {
							e1.printStackTrace();
							throw new DeviceException("createdDate for filter is not in correct format" + value);
						}
						Instant date = Instant.parse(DATE_TIME_FORMATTER.format(d2.toInstant()));
						predicates.add(criteriaBuilder.between(root.get("createdDate"), date,
								date.plus(1439, ChronoUnit.MINUTES)));
					}

					if (attribute.equalsIgnoreCase("serviceTime")) {

						Date d2 = null;
						try {
							d2 = dateFormat.parse(value);
						} catch (ParseException e1) {
							e1.printStackTrace();
							throw new DeviceException("serviceTime for filter is not in correct format" + value);
						}
						Instant date = Instant.parse(DATE_TIME_FORMATTER.format(d2.toInstant()));
						predicates.add(criteriaBuilder.between(root.get("serviceDateTime"), date,
								date.plus(1439, ChronoUnit.MINUTES)));
					}

//					if (attribute.equalsIgnoreCase("serviceTime")) {
//						predicates.add(criteriaBuilder.or(criteriaBuilder.like(root.get("serviceDateTime"), "%" + value + "%"), criteriaBuilder.like(root.get("user").get("lastName"), "%" + value + "%")));
//					}

					if (attribute.equalsIgnoreCase("technician")) {
						predicates.add(criteriaBuilder.or(
								criteriaBuilder.like(root.get("user").get("firstName"), "%" + value + "%"),
								criteriaBuilder.like(root.get("user").get("lastName"), "%" + value + "%")));
					}

					if (attribute.equalsIgnoreCase("newSensorId")) {
						predicates.add(criteriaBuilder.like(root.get("newSensorId"), "%" + value + "%"));
					}

					if (attribute.equalsIgnoreCase("oldSensorId")) {
						predicates.add(criteriaBuilder.like(root.get("oldSensorId"), "%" + value + "%"));
					}

					if (attribute.equalsIgnoreCase("resolutionType")) {
						predicates.add(criteriaBuilder.like(root.get("resolutionType"), "%" + value + "%"));
					}

					if (attribute.equalsIgnoreCase("deviceId")) {
						predicates.add(criteriaBuilder.like(root.get("device").get("imei"), "%" + value + "%"));
					}

					if (attribute.equalsIgnoreCase("assetId")) {
						predicates.add(criteriaBuilder.like(root.get("asset").get("assignedName"), "%" + value + "%"));
					}

					if (attribute.equalsIgnoreCase("workOrder")) {
						predicates.add(criteriaBuilder.like(root.get("workOrder"), "%" + value + "%"));
					}

					if (attribute.equalsIgnoreCase("maintenanceLocation")) {
						if (value.equalsIgnoreCase("null")) {
							predicates.add(criteriaBuilder.isNull(root.get("maintenanceLocation")));
						} else {
							String[] maintenanceLocationArray = value.split(",");
							List<String> maintenanceLocationList = Arrays.asList(maintenanceLocationArray);
							List<Predicate> maintenanceLocationPredicates = new ArrayList<>();
							for (String maintenanceLocation : maintenanceLocationList) {
								if (maintenanceLocation != null && !maintenanceLocation.isEmpty()) {
									maintenanceLocationPredicates.add(
											criteriaBuilder.like(criteriaBuilder.lower(root.get("maintenanceLocation")),
													"%" + maintenanceLocation.toLowerCase() + "%"));
								}
							}
							if (!maintenanceLocationPredicates.isEmpty()) {
								predicates.add(
										criteriaBuilder.or(maintenanceLocationPredicates.toArray(new Predicate[0])));
							}
						}
					}

					if (attribute.equalsIgnoreCase("sensorType")) {
						if (value.equalsIgnoreCase("null")) {
							predicates.add(criteriaBuilder.isNull(root.get("sensorType")));
						} else {
							String[] sensorTypeArray = value.split(",");
							List<String> sensorTypeList = Arrays.asList(sensorTypeArray);
							List<Predicate> sensorTypePredicates = new ArrayList<>();
							for (String sensorTypeName : sensorTypeList) {
								if (sensorTypeName != null && !sensorTypeName.isEmpty()) {
									sensorTypePredicates
											.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("sensorType")),
													"%" + sensorTypeName.toLowerCase() + "%"));
								}
							}
							if (!sensorTypePredicates.isEmpty()) {
								predicates.add(criteriaBuilder.or(sensorTypePredicates.toArray(new Predicate[0])));
							}
						}
					}
					
					if (attribute.equalsIgnoreCase("uuidList")) {
						if (value.equalsIgnoreCase("null")) {
							predicates.add(criteriaBuilder.isNull(root.get("uuid")));
						} else {
							String[] uuId = value.split(",");
							List<String> uuIdList = Arrays.asList(uuId);
							List<Predicate> uuIdPredicates = new ArrayList<>();
							for (String uuid : uuIdList) {
								if (uuid != null && !uuid.isEmpty()) {
									uuIdPredicates.add(
											criteriaBuilder.equal(root.get("uuid"), uuid.trim()));
								}
							}
							if (!uuIdPredicates.isEmpty()) {
								predicates.add(criteriaBuilder.or(uuIdPredicates.toArray(new Predicate[0])));
							}
						}
					}

					if (attribute.equalsIgnoreCase("serviceVendorName")) {
						if (value.equalsIgnoreCase("null")) {
							predicates.add(criteriaBuilder.isNull(root.get("organisation").get("organisationName")));
						} else {
							String[] serviceVendorNameArray = value.split(",");
							List<String> serviceVendorNameList = Arrays.asList(serviceVendorNameArray);
							List<Predicate> sensorTypePredicates = new ArrayList<>();
							for (String serviceVendorName : serviceVendorNameList) {
								if (serviceVendorName != null && !serviceVendorName.isEmpty()) {
									sensorTypePredicates.add(criteriaBuilder.like(
											criteriaBuilder.lower(root.get("organisation").get("organisationName")),
											"%" + serviceVendorName.toLowerCase() + "%"));
								}
							}
							if (!sensorTypePredicates.isEmpty()) {
								predicates.add(criteriaBuilder.or(sensorTypePredicates.toArray(new Predicate[0])));
							}
						}
					}

					if (attribute.equalsIgnoreCase("validationTime")) {
						if (value.equalsIgnoreCase("null")) {
							predicates.add(criteriaBuilder.isNull(root.get("validationTime")));
						} else {
							String[] validationTimeArray = value.split(",");
							List<String> validationTimeList = Arrays.asList(validationTimeArray);
							List<Predicate> validationTimePredicates = new ArrayList<>();
							for (String validationTime : validationTimeList) {
								if (validationTime != null && !validationTime.isEmpty()) {
									validationTimePredicates
											.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("validationTime")),
													"%" + validationTime.toLowerCase() + "%"));
								}
							}
							if (!validationTimePredicates.isEmpty()) {
								predicates.add(criteriaBuilder.or(validationTimePredicates.toArray(new Predicate[0])));
							}
						}
					}
				}
			});
			return query.where(criteriaBuilder.and(predicates.toArray(new Predicate[0]))).getRestriction();
		};
	}

}
