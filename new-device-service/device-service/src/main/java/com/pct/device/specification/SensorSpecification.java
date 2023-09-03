package com.pct.device.specification;

import com.pct.common.constant.SensorStatus;
import com.pct.common.model.Sensor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public final class SensorSpecification {

    private SensorSpecification() {
    }

    public static Specification<Sensor> getSensorListSpecification(String accountNumber, String sensorUuid,
                                                                   SensorStatus sensorStatus) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList();

            if (!StringUtils.isEmpty(sensorUuid)) {
                predicates.add(criteriaBuilder.equal(root.get("uuid"), sensorUuid));
            }

            if (!StringUtils.isEmpty(sensorStatus)) {
                predicates.add(criteriaBuilder.equal(root.get("status"), sensorStatus));
            }

            if (!StringUtils.isEmpty(accountNumber)) {
                predicates.add(
                        criteriaBuilder.equal(root.get("gateway").get("company").get("accountNumber"), accountNumber));
            }

            return query.where(criteriaBuilder.and(predicates.toArray(new Predicate[0]))).getRestriction();
        };
    }
}