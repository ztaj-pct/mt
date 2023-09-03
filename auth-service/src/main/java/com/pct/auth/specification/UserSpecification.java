package com.pct.auth.specification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.CriteriaBuilder.In;

import org.springframework.data.jpa.domain.Specification;

import com.pct.auth.util.StringUtils;
import com.pct.common.constant.Constants;
import com.pct.common.model.Role;
import com.pct.common.model.User;

public class UserSpecification {

	public static Specification<User> getDeviceListSpecification(Map<String, String> attributeToValueMap,
			String filterModelCountFilter, User user, String sort) {
		return (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<Predicate>();

			boolean roleCallCenter = false;
			boolean roleOrgAdmin = false;
			boolean roleMaintenance = false;
			for (Role roles : user.getRole()) {
				if (roles.getName().contains(Constants.ROLE_CALL_CENTER_USER)) {
					roleCallCenter = true;
					break;
				}
				if (roles.getName().contains(Constants.ROLE_CUSTOMER_ADMIN)||roles.getName().contains(Constants.ROLE_ORGANIZATION_USER)) {
					roleOrgAdmin = true;
					break;
				}
				if (roles.getName().contains(Constants.ROLE_MAINTENANCE)) {
					roleMaintenance = true;
					break;
				}
			}
			final boolean callCenterRole = roleCallCenter;
			
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
			
			if (callCenterRole) {
				predicates.add(criteriaBuilder.equal(root.get("organisation").get("accountNumber"), "A-00243"));
				predicates.add(criteriaBuilder.equal(root.join("role").get("name"), Constants.ROLE_CALL_CENTER_USER));
			}
			
			if (roleMaintenance) {
				predicates.add(criteriaBuilder.equal(root.join("role").get("name"), Constants.ROLE_MAINTENANCE));
			}
			
			if (roleOrgAdmin && user.getOrganisation() != null) {
				predicates.add(criteriaBuilder.equal(root.get("organisation").get("accountNumber"),
						user.getOrganisation().getAccountNumber()));
			}
			attributeToValueMap.forEach((attribute, value) -> {
				if (!StringUtils.isEmpty(value)) {

					if (attribute.equalsIgnoreCase("userName")) {
						predicates.add(criteriaBuilder.like(root.get("userName"), "%" + value + "%"));
					}

					if (attribute.equalsIgnoreCase("email")) {
						predicates.add(criteriaBuilder.like(root.get("email"), "%" + value + "%"));
					}

					if (attribute.equalsIgnoreCase("phone")) {
						predicates.add(criteriaBuilder.like(root.get("phone"), "%" + value + "%"));
					}

					if (attribute.equalsIgnoreCase("firstName")) {
						predicates.add(criteriaBuilder.like(root.get("firstName"), "%" + value + "%"));
					}
					if (attribute.equalsIgnoreCase("lastName")) {
						predicates.add(criteriaBuilder.like(root.get("lastName"), "%" + value + "%"));
					}
					if (!callCenterRole) {
						if (attribute.equalsIgnoreCase("organisationName")) {
							predicates.add(criteriaBuilder.like(root.get("organisation").get("organisationName"),
									"%" + value + "%"));
						}
					}

					if (attribute.equalsIgnoreCase("isActive")) {
						if (value.equalsIgnoreCase("null")) {
							predicates.add(criteriaBuilder.isTrue(criteriaBuilder.literal(false)));
						} else if (Boolean.parseBoolean(value)) {
							predicates.add(criteriaBuilder.isTrue(root.get("isActive")));
						} else {
							predicates.add(criteriaBuilder.isFalse(root.get("isActive")));
						}
					}

					if (attribute.equalsIgnoreCase("roleName")) {

						List<Predicate> criteriaStatus = new ArrayList<Predicate>();
						String[] allStatus = value.split(",");
						List<String> list = Arrays.asList(allStatus);
						In<Object> in = criteriaBuilder.in(root.join("role").get("name"));
						in.value(list);
						criteriaStatus.add(in);
						Predicate[] p = new Predicate[criteriaStatus.size()];
						predicates.add(criteriaBuilder.or(criteriaStatus.toArray(p)));
					}

				}
			});
			return query.where(criteriaBuilder.and(predicates.toArray(new Predicate[0]))).getRestriction();
		};
	}
}
