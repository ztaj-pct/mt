package com.pct.auth.specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import com.pct.auth.util.StringUtils;
import com.pct.common.model.Role;

public class RoleSpecification {
	public static Specification<Role> getDeviceListSpecification(Map<String, String> attributeToValueMap,
			String filterModelCountFilter) {
		return (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<Predicate>();

			attributeToValueMap.forEach((attribute, value) -> {
				if (!StringUtils.isEmpty(value)) {

					if (attribute.equalsIgnoreCase("name")) {
						predicates.add(criteriaBuilder.like(root.get("name"), "%" + value + "%"));
					}

					if (attribute.equalsIgnoreCase("description")) {
						predicates.add(criteriaBuilder.like(root.get("description"), "%" + value + "%"));
					}
				}
			});
			return query.where(criteriaBuilder.and(predicates.toArray(new Predicate[0]))).getRestriction();
		};
	}
}
