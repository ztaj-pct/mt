package com.pct.installer.specification;

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

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.CriteriaBuilder.In;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.pct.common.constant.AssetCategory;
import com.pct.common.constant.AssetStatus;
import com.pct.common.model.InstallHistory;
import com.pct.common.model.Role;
import com.pct.common.model.User;
import com.pct.installer.exception.InstallationException;
import com.pct.installer.util.AuthoritiesConstants;

public final class InstallerHistorySpecification {

	  private InstallerHistorySpecification() {
	    }	
	
	  
		static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		public static final TimeZone timeZoneUTC = TimeZone.getTimeZone("PST");
		static {
			dateFormat.setTimeZone(timeZoneUTC);
		}
		static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
	            .withZone(ZoneId.of("America/Los_Angeles"));

		
		
	public static Specification<InstallHistory> getaInstallerSpecification(Map<String, String> attributeToValueMap, User user,String companyId, long days) {
		return (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList();
			List<String> role = user.getRole().stream().map(Role::getName).collect(Collectors.toList());
			if ((role.contains(AuthoritiesConstants.SUPER_ADMIN) || role.contains(AuthoritiesConstants.CUSTOMER_ADMIN) || role.contains(AuthoritiesConstants.INSTALLER)) && companyId != null) {
				predicates.add(criteriaBuilder.equal(root.get("organisation").get("uuid"), companyId));
				if(companyId!=null && days!=0) {
					Date d = new Date();
					Instant date = Instant.parse(DATE_TIME_FORMATTER.format(d.toInstant()));
					predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dateStarted"), date.minus(days,ChronoUnit.DAYS)));
				}
			}else {
				predicates.add(criteriaBuilder.equal(root.get("organisation").get("id"), user.getOrganisation().getId()));
			}
			attributeToValueMap.forEach((attribute, value) -> {
				if (!StringUtils.isEmpty(value)) {
					if (attribute.equalsIgnoreCase("installed")) {

						Date d2 = null;
						try {
							d2 = dateFormat.parse(value);
						} catch (ParseException e1) {
							e1.printStackTrace();
							throw new InstallationException("createdAt for filter is not in correct format" + value);
						}
						Instant date = Instant.parse(DATE_TIME_FORMATTER.format(d2.toInstant()));
						predicates.add(
								criteriaBuilder.between(root.get("dateEnded"), date, date.plus(1, ChronoUnit.DAYS)));
					} 
					
					if (attribute.equalsIgnoreCase("dateStarted")) {

						Date d2 = null;
						try {
							d2 = dateFormat.parse(value);
						} catch (ParseException e1) {
							e1.printStackTrace();
							throw new InstallationException("createdAt for filter is not in correct format" + value);
						}
						Instant date = Instant.parse(DATE_TIME_FORMATTER.format(d2.toInstant()));
						predicates.add(criteriaBuilder.between(root.get("dateStarted"), date, date.plus(1, ChronoUnit.DAYS)));
					} 
					
					if (attribute.equalsIgnoreCase("createdAt")) {

						Date d2 = null;
						try {
							d2 = dateFormat.parse(value);
						} catch (ParseException e1) {
							e1.printStackTrace();
							throw new InstallationException("createdAt for filter is not in correct format" + value);
						}
						Instant date = Instant.parse(DATE_TIME_FORMATTER.format(d2.toInstant()));
						predicates.add(
								criteriaBuilder.between(root.get("createdOn"), date, date.plus(1, ChronoUnit.DAYS)));
					} 
					if (attribute.equalsIgnoreCase("updatedAt")) {
						Date d2 = null;
						try {
							d2 = dateFormat.parse(value);
						} catch (ParseException e1) {
							e1.printStackTrace();
							throw new InstallationException("createdAt for filter is not in correct format" + value);
						}
						Instant date = Instant.parse(DATE_TIME_FORMATTER.format(d2.toInstant()));
						predicates.add(
								criteriaBuilder.between(root.get("updatedOn"), date, date.plus(1, ChronoUnit.DAYS)));
					} 
					
					if (attribute.equalsIgnoreCase("assetId")) {
						predicates.add(criteriaBuilder.like(root.get("asset").get("assignedName"), "%" + value + "%"));
						} 
					
					if (attribute.equalsIgnoreCase("status")) {
//						if(value.equalsIgnoreCase("null")) 
//							predicates.add(criteriaBuilder.isTrue(criteriaBuilder.literal(false)));
						List<Predicate> criteriaStatus = new ArrayList<Predicate>();
						String[] allStatus = value.split(",");
						for (String status : allStatus) {
							if (status.equalsIgnoreCase(AssetStatus.ACTIVE.toString()))
								criteriaStatus.add(criteriaBuilder.equal(root.get("asset").get("status"), AssetStatus.ACTIVE));
							else if (status.equalsIgnoreCase(AssetStatus.INACTIVE.toString()))
								criteriaStatus.add(criteriaBuilder.equal(root.get("asset").get("status"), AssetStatus.INACTIVE));
							else if (status.equalsIgnoreCase(AssetStatus.DELETED.toString()))
								criteriaStatus.add(criteriaBuilder.equal(root.get("asset").get("status"), AssetStatus.DELETED));
							else if (status.equalsIgnoreCase(AssetStatus.ERROR.toString()))
								criteriaStatus.add(criteriaBuilder.equal(root.get("asset").get("status"), AssetStatus.ERROR));
							else if (status.equalsIgnoreCase("Install In Progress"))
								criteriaStatus.add(criteriaBuilder.equal(root.get("asset").get("status"), AssetStatus.INSTALL_IN_PROGRESS));
							else if (status.equalsIgnoreCase(AssetStatus.PARTIAL.toString()))
								criteriaStatus.add(criteriaBuilder.equal(root.get("asset").get("status"), AssetStatus.PARTIAL));
							else if (status.equalsIgnoreCase(AssetStatus.PENDING.toString()))
								criteriaStatus.add(criteriaBuilder.equal(root.get("asset").get("status"), AssetStatus.PENDING));
						}
						predicates.add(criteriaBuilder.or(criteriaStatus.toArray(new Predicate[0])));
					}
						 
					if (attribute.equalsIgnoreCase("productName")) {
						System.out.println("inside eligible_gateway");
						
						
						List<Predicate> criteriaStatus = new ArrayList<Predicate>();
						String[] allStatus = value.split(",");
						List<String> list = Arrays.asList(allStatus);
						In<Object> in = criteriaBuilder.in(root.get("device").get("productName"));
						in.value(list) ;
						criteriaStatus.add(in) ;
						
						Predicate[] p = new Predicate[criteriaStatus.size()];  
						predicates.add(criteriaBuilder.or(criteriaStatus.toArray(p)));
						
						} 
					if (attribute.equalsIgnoreCase("installer_name")) {
						Predicate concatFields = criteriaBuilder.like(
								criteriaBuilder.concat(root.get("createdBy").get("firstName"),root.get("createdBy").get("lastName")),   
								"%" + value + "%");
						predicates.add(concatFields);
						}
					if (attribute.equalsIgnoreCase("installer_company")) {
						predicates.add(criteriaBuilder.like(root.get("createdBy").get("organisation").get("organisationName"), "%" + value + "%"));
						}
					
					
					if (attribute.equalsIgnoreCase("deviceId")) {
						predicates.add(criteriaBuilder.like(root.get("device").get("imei"), "%" + value + "%"));
						}
					
					if (attribute.equalsIgnoreCase("app_version")) {
						predicates.add(criteriaBuilder.like(root.get("appVersion"), "%" + value + "%"));
						}
		
				}
			});
			return query.where(criteriaBuilder.and(predicates.toArray(new Predicate[0]))).getRestriction();
		};

	}

}
