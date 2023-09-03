package com.pct.organisation.specification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.persistence.criteria.CommonAbstractCriteria;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.pct.common.constant.OrganisationRole;
import com.pct.common.model.Organisation;
//import com.pct.common.model.OrganisationRole;
import com.pct.common.model.Role;
import com.pct.common.model.User;
import com.pct.organisation.util.AuthoritiesConstants;

public final class OrganisationSpecification {

	private OrganisationSpecification() {
	}

	public static Specification<Organisation> getOrganisationSpecification(Map<String, String> attributeToValueMap,
			User user, String sort) {
		return (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();

			boolean roleAvailable = false;
			if (user.getRole() != null) {
				for (Role roles : user.getRole()) {
					if (roles.getName().contains(AuthoritiesConstants.SUPER_ADMIN)||roles.getName().contains(AuthoritiesConstants.ROLE_PCT_CONNECT)) {
						roleAvailable = true;
						break;
					}
			
				}
			}

			if (!roleAvailable) {
				predicates.add(criteriaBuilder.equal(root.get("id"), user.getOrganisation().getId()));
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
			
			Set<OrganisationRole> yOrganisationRoles = new HashSet<>();
			Set<OrganisationRole> nOrganisationRoles = new HashSet<>();
			List<String> roleSortParams = new ArrayList<>();
			attributeToValueMap.forEach((attribute, value) -> {
				
				if (StringUtils.hasText(value)) {
					if (attribute.equalsIgnoreCase("organisationName")) {
						predicates.add(criteriaBuilder.like(root.get("organisationName"), "%" + value + "%"));
					}
					
					if (attribute.equalsIgnoreCase("resellers")) {
						if (value.equalsIgnoreCase("null")) {
							predicates.add(
									criteriaBuilder.isEmpty(root.get("resellerList")));
						} else {
							String[] resellersArray = value.split(",");
							List<String> resellersList = Arrays.asList(resellersArray);
							predicates.add(criteriaBuilder.in(root.join("resellerList").get("organisationName"))
									.value(resellersList));
						}
					}

					if (attribute.equalsIgnoreCase("forwardingGroup")) {
						if (value.equalsIgnoreCase("null")) {
							predicates.add(criteriaBuilder.isEmpty(root.get("forwardingGroupMappers")));
						} else {
							String[] forwardingGroupArray = value.split(",");
							List<String> forwardingGroupList = Arrays.asList(forwardingGroupArray);
							predicates.add(criteriaBuilder
									.in(root.join("forwardingGroupMappers").get("customerForwardingGroup").get("name"))
									.value(forwardingGroupList));
						}
					}
					
					if (attribute.equalsIgnoreCase("canView")) {
						List<Predicate> criteriaStatus = new ArrayList<Predicate>();
						String[] allStatus = value.split(",");
						List<String> list = Arrays.asList(allStatus);
						In<Object> in = criteriaBuilder.in(root.join("accessList").get("organisationName"));
						in.value(list);
						criteriaStatus.add(in);

						Predicate[] p = new Predicate[criteriaStatus.size()];
						predicates.add(criteriaBuilder.or(criteriaStatus.toArray(p)));

					}

					if (attribute.equalsIgnoreCase("isAssetListRequired")) {

						List<Predicate> criteriaStatus = new ArrayList<Predicate>();
						String[] allStatus = value.split(",");
						for (String status : allStatus) {
							if (status.equalsIgnoreCase("No"))
								criteriaStatus
										.add(criteriaBuilder.equal(root.get("isAssetListRequired"), Boolean.FALSE));
							if (status.equalsIgnoreCase("Yes"))
								criteriaStatus
										.add(criteriaBuilder.equal(root.get("isAssetListRequired"), Boolean.TRUE));
							if (status.equalsIgnoreCase("N/A"))
								criteriaStatus
										.add(criteriaBuilder.isNull(root.get("isAssetListRequired")));
						}
						predicates.add(criteriaBuilder.or(criteriaStatus.toArray(new Predicate[0])));

					}
					if (("status").equalsIgnoreCase(attribute)) {
						if (value.equalsIgnoreCase("null"))
							predicates.add(criteriaBuilder.isTrue(criteriaBuilder.literal(false)));

						if (value.equalsIgnoreCase("active"))
							predicates.add(criteriaBuilder.equal(root.get("isActive"), Boolean.TRUE));

						if (value.equalsIgnoreCase("inactive"))
							predicates.add(criteriaBuilder.equal(root.get("isActive"), Boolean.FALSE));

					}

					if (attribute.equalsIgnoreCase("endCustomer")) {
						if (!value.equalsIgnoreCase("null")) {
							if (value.equalsIgnoreCase("Yes")) {
								yOrganisationRoles.add(OrganisationRole.END_CUSTOMER);
							} else {
								nOrganisationRoles.add(OrganisationRole.END_CUSTOMER);
							}
						} else {
							predicates.add(criteriaBuilder.isTrue(criteriaBuilder.literal(false)));
						}
					}

					if (attribute.equalsIgnoreCase("reseller")) {
						if (!value.equalsIgnoreCase("null")) {
							if (value.equalsIgnoreCase("Yes")) {
								yOrganisationRoles.add(OrganisationRole.RESELLER);
							} else {
								nOrganisationRoles.add(OrganisationRole.RESELLER);
							}
						} else {
							predicates.add(criteriaBuilder.isTrue(criteriaBuilder.literal(false)));
						}
					}

					if (attribute.equalsIgnoreCase("installer")) {
						if (!value.equalsIgnoreCase("null")) {
							if (value.equalsIgnoreCase("Yes")) {
								yOrganisationRoles.add(OrganisationRole.INSTALLER);
							} else {
								nOrganisationRoles.add(OrganisationRole.INSTALLER);
							}
						} else {
							predicates.add(criteriaBuilder.isTrue(criteriaBuilder.literal(false)));
						}
					}
					
					if (attribute.equalsIgnoreCase("maintenance_mode")) {
						if (!value.equalsIgnoreCase("null")) {
							if (value.equalsIgnoreCase("Yes")) {
								yOrganisationRoles.add(OrganisationRole.MAINTENANCE_MODE);
							} else {
								nOrganisationRoles.add(OrganisationRole.MAINTENANCE_MODE);
							}
						} else {
							predicates.add(criteriaBuilder.isTrue(criteriaBuilder.literal(false)));
						}
					}
					if (attribute.equalsIgnoreCase("roleSort")) {
						if (value != null && !value.isEmpty() && !value.equalsIgnoreCase("null")) {
							String splitValue[] = value.split(Pattern.quote(","));
							if(splitValue.length >= 1) {
								roleSortParams.add(splitValue[0]);  
							}
							if(splitValue.length == 2) {
								roleSortParams.add(splitValue[1]);  
							}
						}
					}
				}
			});

			yOrganisationRoles.forEach(y->{
				predicates.add(root.get("id")
						.in(getYNOrganisationRolesSubquery(y, criteriaBuilder, query, root)));
			});	

			nOrganisationRoles.forEach(n->{
				predicates.add(criteriaBuilder.not(root.get("id")
						.in(getYNOrganisationRolesSubquery(n, criteriaBuilder, query, root))));
			});
			
			if(!roleSortParams.isEmpty() ) {
				
			Expression<Object> caseExpression = criteriaBuilder.
						selectCase().
						when(criteriaBuilder.equal(
								getCountOrganisationRolesSubquery(OrganisationRole.valueOf(roleSortParams.get(0)), criteriaBuilder, query, root), 1), 2).
						otherwise(1);
				Order ord = criteriaBuilder.asc(caseExpression);
				if(roleSortParams.size() == 2 && "desc".equalsIgnoreCase(roleSortParams.get(1))) {
					ord = criteriaBuilder.desc(caseExpression);
				}
				query.orderBy(ord);
				
			}

			return !predicates.isEmpty()
					? query.where(criteriaBuilder.and(predicates.toArray(new Predicate[0]))).getRestriction()
					: null;
		};
	}

	private static Subquery<Long> getYNOrganisationRolesSubquery(
			OrganisationRole organisationRole,
			CriteriaBuilder criteriaBuilder, CommonAbstractCriteria query, Root<Organisation> root) {
		Subquery<Long> subquery = query.subquery(Long.class);
		Root<Organisation> subRoot = subquery.from(Organisation.class);
		Join<Organisation, OrganisationRole> organisationRoleJoin = subRoot.join("organisationRole", JoinType.INNER);

		subquery.distinct(true).select(subRoot.get("id"));

		List<Predicate> subqueryPredicates = new ArrayList<>();

		subqueryPredicates.add(criteriaBuilder.equal(root.get("id"), subRoot.get("id")));

		subqueryPredicates.add(criteriaBuilder.equal(organisationRoleJoin, organisationRole));

		subquery.where(subqueryPredicates.toArray(new Predicate[0]));
		
		return subquery;
	}
	
	private static Subquery<Long> getCountOrganisationRolesSubquery(
			OrganisationRole organisationRole,
			CriteriaBuilder criteriaBuilder, 
			CommonAbstractCriteria query, 
			Root<Organisation> root) {
		Subquery<Long> subquery = query.subquery(Long.class);
		Root<Organisation> subRoot = subquery.from(Organisation.class);
		Join<Organisation, OrganisationRole> organisationRoleJoin = subRoot.join("organisationRole", JoinType.INNER);

		subquery.distinct(true).select(criteriaBuilder.count(subRoot.get("id")));

		List<Predicate> subqueryPredicates = new ArrayList<>();

		subqueryPredicates.add(criteriaBuilder.equal(root.get("id"), subRoot.get("id")));

		subqueryPredicates.add(criteriaBuilder.equal(organisationRoleJoin, organisationRole));

		subquery.where(subqueryPredicates.toArray(new Predicate[0]));
		
		return subquery;
	}
	
}
