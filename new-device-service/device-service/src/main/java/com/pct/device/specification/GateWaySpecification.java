package com.pct.device.specification;

import com.pct.common.constant.GatewayStatus;
import com.pct.common.constant.GatewayType;
import com.pct.common.model.Gateway;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class GateWaySpecification {

    private GateWaySpecification() {
    }

    public static Specification<Gateway> getGatewayListSpecification(String accountNumber, String imei, String gatewayUuid, GatewayStatus status, GatewayType type, String mac) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList();

            if (!StringUtils.isEmpty(imei)) {
                predicates.add(criteriaBuilder.equal(root.get("imei"), imei));
            }

            if (!StringUtils.isEmpty(status)) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            if (!StringUtils.isEmpty(gatewayUuid)) {
                predicates.add(criteriaBuilder.equal(root.get("uuid"), gatewayUuid));
            }

            if (!StringUtils.isEmpty(accountNumber)) {
                predicates.add(criteriaBuilder.equal(root.get("company").get("accountNumber"), accountNumber));
            }
            if (!StringUtils.isEmpty(mac)) {
                predicates.add(criteriaBuilder.equal(root.get("macAddress"), mac));
            }
            if (!StringUtils.isEmpty(type)) {
                predicates.add(criteriaBuilder.equal(root.get("type"), type));
            }

            return query.where(criteriaBuilder.and(predicates.toArray(new Predicate[0]))).getRestriction();
        };
    }

    public static Specification<Gateway> getSpecification(Map<String, String> attributeToValueMap) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList();
            attributeToValueMap.forEach((attribute, value) -> {
                if(!StringUtils.isEmpty(value)) {
                    predicates.add(criteriaBuilder.equal(root.get(attribute), value));
                }
            });
            return query.where(criteriaBuilder.and(predicates.toArray(new Predicate[0]))).getRestriction();
        };

    }
}