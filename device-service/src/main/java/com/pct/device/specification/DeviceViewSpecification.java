package com.pct.device.specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import com.pct.common.constant.DeviceStatus;
import com.pct.common.constant.GatewayStatus;
import com.pct.common.constant.IOTType;
import com.pct.common.model.Device;
import com.pct.device.model.DeviceView;
import com.pct.device.util.StringUtils;

public final class DeviceViewSpecification {

	public static Specification<DeviceView> getGatewayListSpecification(String accountNumber, String imei,
			String gatewayUuid, DeviceStatus status, IOTType type, String mac, Instant lastDownloadeTime) {
		return (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();

			// added condition for not deleted records
			// predicates.add(criteriaBuilder.equal(root.get("isDeleted"), Boolean.FALSE));

			if (!StringUtils.isEmpty(imei)) {
				predicates.add(criteriaBuilder.equal(root.get("imei"), imei));
			}

			if (!StringUtils.isEmpty(status)) {
				predicates.add(criteriaBuilder.equal(root.get("status"), status.getValue()));
			}
			else{
				if(StringUtils.isEmpty(imei)){
				predicates.add(criteriaBuilder.or(criteriaBuilder.equal(root.get("status"), "PENDING"),
				criteriaBuilder.equal(root.get("status"), "INSTALL_IN_PROGRESS")));
				}
			}

			if (!StringUtils.isEmpty(gatewayUuid)) {
				predicates.add(criteriaBuilder.equal(root.get("uuid"), gatewayUuid));
			}

			if (!StringUtils.isEmpty(accountNumber)) {
				predicates.add(criteriaBuilder.equal(root.get("can"), accountNumber));
			}
			if (!StringUtils.isEmpty(mac)) {
				predicates.add(criteriaBuilder.equal(root.get("macAddress"), mac));
			}
			if (!StringUtils.isEmpty(type)) {
				predicates.add(criteriaBuilder.equal(root.get("iotType"), type.getIOTTypeValue()));
			}

			if (lastDownloadeTime != null) {
				predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("timeOfLastDownload"), lastDownloadeTime));
			}

			return query.where(criteriaBuilder.and(predicates.toArray(new Predicate[0]))).getRestriction();
		};
	}

}
