package com.pct.auth.specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import com.pct.auth.util.StringUtils;
import com.pct.common.model.PermissionEntity;

public class PermissionSpecification {
	public static Specification<PermissionEntity> getDeviceListSpecification(Map<String, String> attributeToValueMap,
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

					if (attribute.equalsIgnoreCase("methodType")) {
						predicates.add(criteriaBuilder.like(root.get("methodType"), "%" + value + "%"));
					}
					if (attribute.equalsIgnoreCase("path")) {
						predicates.add(criteriaBuilder.like(root.get("path"), "%" + value + "%"));
					}
				}
			});
			return query.where(criteriaBuilder.and(predicates.toArray(new Predicate[0]))).getRestriction();
		};
	}
}
