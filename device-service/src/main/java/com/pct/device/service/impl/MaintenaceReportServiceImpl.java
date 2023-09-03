package com.pct.device.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.pct.common.model.Asset;
import com.pct.common.model.Device;
import com.pct.common.model.MaintenanceReportHistory;
import com.pct.common.model.SensorDetail;
import com.pct.common.model.User;
import com.pct.device.dto.MaintenanceReportDTO;
import com.pct.device.exception.DeviceException;
import com.pct.device.ms.repository.MaintenanceReportHistoryRepository;
import com.pct.device.payload.MaintenanceReportHistoryPayload;
import com.pct.device.repository.IAssetRepository;
import com.pct.device.repository.IDeviceRepository;
import com.pct.device.repository.SensorDetailRepository;
import com.pct.device.service.IMaintenanceReportService;
import com.pct.device.specification.MaintenanceReportHistorySpecification;
import com.pct.device.util.BeanConverter;
import com.pct.device.util.RestUtils;

@Service
public class MaintenaceReportServiceImpl implements IMaintenanceReportService {

	Logger logger = LoggerFactory.getLogger(MaintenaceReportServiceImpl.class);
	
	@Autowired
	private RestUtils restUtils;
	
	@Autowired
	private MaintenanceReportHistoryRepository maintenanceReporRepository;
	
	@Autowired
	private IDeviceRepository deviceRepository;

	@Autowired
	private SensorDetailRepository sensorDetailRepository;
	
	@Autowired
	private IAssetRepository assetRepository;
	
	@Autowired
	private BeanConverter beanConverter;
	
	
	@Override
	public MaintenanceReportHistory createMaintenaceRportHistory(MaintenanceReportDTO maintenanceReportDTO, String userName) {
		MaintenanceReportHistory maintenanceReportHistory = new MaintenanceReportHistory();
		logger.info("Inside createMaintenaceRportHistory");
		User user = restUtils.getUserFromAuthService(userName);
		Device device = deviceRepository.findByImei(maintenanceReportDTO.getDeviceId());
		Asset asset = assetRepository.findByAssignedName(maintenanceReportDTO.getAssetId());
		if(user != null && device != null && asset != null ) {
			logger.info("Before Saving Maintenance Report History");
			maintenanceReportHistory.setAsset(asset);
			maintenanceReportHistory.setDevice(device);
			maintenanceReportHistory.setNewSensorId(maintenanceReportDTO.getNewSensorId());
			maintenanceReportHistory.setOldSensorId(maintenanceReportDTO.getOldSensorId());
			maintenanceReportHistory.setOrganisation(user.getOrganisation());
			maintenanceReportHistory.setUser(user);
			maintenanceReportHistory.setServiceDateTime(Instant.now());
			maintenanceReportHistory.setValidationTime(maintenanceReportDTO.getValidationTime());
			maintenanceReportHistory.setSensorType(maintenanceReportDTO.getSensorType());
			maintenanceReportHistory.setResolutionType(maintenanceReportDTO.getResolutionType());
			maintenanceReportHistory.setWorkOrder(maintenanceReportDTO.getWorkOrder());
			maintenanceReportHistory.setMaintenanceLocation(maintenanceReportDTO.getMaintenanceLocation());
			maintenanceReportHistory.setPosition(maintenanceReportDTO.getPosition());

			maintenanceReportHistory.setCreatedDate(Instant.now());
			if(maintenanceReportDTO.getSensorUuid() !=null){
				List<SensorDetail> sd = sensorDetailRepository.findListBySensorUuid(maintenanceReportDTO.getSensorUuid());
				if(sd.size()>0){
					logger.info("data for sensor details for sensor uuid  "+sd.size());

				for(SensorDetail s : sd){
					if(s!=null ){
						logger.info("data for sensor details for position  "+s.getPosition());	
						logger.info("data for sensor details for reques position  "+maintenanceReportDTO.getPosition());	
					}
					if(s.getPosition()!=null && maintenanceReportDTO.getPosition() != null && s.getPosition().equalsIgnoreCase(maintenanceReportDTO.getPosition())){
						logger.info("condition is satisfying");	
						s.setOldSensorId(s.getSensorId());
						s.setSensorId(maintenanceReportDTO.getNewSensorId());
						sensorDetailRepository.save(s);

					}
				}
			}
				Device d = deviceRepository.findByUuid(maintenanceReportDTO.getSensorUuid());
				     if(d != null && maintenanceReportDTO.getMacaddress() !=null){
						d.setOldMacAddress(d.getMacAddress());
						maintenanceReportHistory.setOldMacAddress(d.getMacAddress());
						d.setMacAddress(maintenanceReportDTO.getMacaddress());
						deviceRepository.save(d);
						maintenanceReportHistory.setMacAddress(maintenanceReportDTO.getMacaddress());	
					 }	
					 maintenanceReportHistory.setSensorUuid(maintenanceReportDTO.getSensorUuid());	 
			}
			boolean isMaintenanceReporUuidUnique = false;
			String maintenanceReporUuid = "";
			while (!isMaintenanceReporUuidUnique) {
				maintenanceReporUuid = UUID.randomUUID().toString();
				MaintenanceReportHistory byUuid = maintenanceReporRepository.findByUuid(maintenanceReporUuid);
				if (byUuid == null) {
					isMaintenanceReporUuidUnique = true;
				}
			}
			maintenanceReportHistory.setUuid(maintenanceReporUuid);
			maintenanceReportHistory = maintenanceReporRepository.save(maintenanceReportHistory);
			logger.info("Maintenance Report History Created Successfully");
		} else {
			throw new DeviceException("Comapany/Device/Asset/User can not be null");
		}
		return maintenanceReportHistory;
	}


	@Override
	public Page<MaintenanceReportHistoryPayload> getMaintenanceReportHistoryList(Pageable pageable, String userName,
			Map<String, String> filterValues, String filterModelCountFilter, String organisationName, Integer days,  String sort,boolean isUuid) {


		Page<MaintenanceReportHistory> maintenanceReportHistorys = null;
		User user = restUtils.getUserFromAuthService(userName);
		Specification<MaintenanceReportHistory> spc = MaintenanceReportHistorySpecification.getMaintenanceReportHistorySpecification(filterValues, user,
				filterModelCountFilter, organisationName, days, sort,isUuid);
		maintenanceReportHistorys = maintenanceReporRepository.findAll(spc,pageable);
		return beanConverter
				.convertMaintenanceReportHistoryToMaintenanceReportHistoryPayload(maintenanceReportHistorys, pageable,isUuid);
	}


	@Override
	public List<MaintenanceReportHistoryPayload> getMaintenanceReportHistoryByUuid(List<String> uuidList) {
		List<MaintenanceReportHistory> maintenanceReportHistorys = maintenanceReporRepository.findByUuidList(uuidList);
		return beanConverter
				.convertMaintenanceReportHistoryToMaintenanceReportHistoryPayload(maintenanceReportHistorys);
	}

}
