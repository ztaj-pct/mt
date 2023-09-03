package com.pct.device.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pct.common.constant.DeviceStatus;
import com.pct.device.exception.DeviceException;
import com.pct.device.payload.BeaconDetailPayLoad;
import com.pct.device.payload.BeaconPayload;
import com.pct.device.payload.BeaconRequestPayload;

public interface IBeaconService {

	Boolean addBeaconDetail(BeaconRequestPayload beaconRequestRequest, String userName) throws DeviceException;

	Page<BeaconPayload> getBeaconWithPagination(String accountNumber, String uuid, DeviceStatus status, String mac,
			Map<String, String> filterValues, Pageable pageable);

	boolean deleteBeaconDetail(String can, String uuid);

	Boolean updateBeaconDetail(BeaconDetailPayLoad beaconDetailPayload, String userName);

	List<BeaconPayload> getBeaconDetails(String accountNumber, String uuid);
}
