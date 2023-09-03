package com.pct.device.version.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pct.common.model.Role;
import com.pct.common.model.User;
import com.pct.device.version.exception.BadRequestException;
import com.pct.device.version.exception.DeviceVersionException;
import com.pct.device.version.model.EmergencyStop;
import com.pct.device.version.model.Package;
import com.pct.device.version.payload.PackagePayload;
import com.pct.device.version.payload.SavePackageRequest;
import com.pct.device.version.repository.ICampaignStepRepository;
import com.pct.device.version.repository.IEmergencyStopRepository;
import com.pct.device.version.repository.IPackageRepository;
import com.pct.device.version.service.IPackageService;
import com.pct.device.version.specification.CampaignSpecification;
import com.pct.device.version.util.BeanConverter;
import com.pct.device.version.util.Constants;
import com.pct.device.version.util.RestUtils;
import com.pct.device.version.validation.AuthoritiesConstants;

@Service
public class PackageServiceImpl implements IPackageService {

	Logger logger = LoggerFactory.getLogger(PackageServiceImpl.class);
	
    @Autowired
    private IPackageRepository packageRepository;
    @Autowired
	private RestUtils restUtils;
    @Autowired
	private BeanConverter beanConverter;
    @Autowired
	private ICampaignStepRepository campaignStepRepository;
    
    @Autowired
	private IEmergencyStopRepository emergencyStopRepository;
    
    
    @Override
	public void savePackage(List<SavePackageRequest> savePackageRequestList, String userName) {

		List<Package> packageListToSave = new ArrayList<Package>();
		Package inputPackage = null;
		User user = restUtils.getUserFromAuthService(userName);
		if (user != null) {
			List<String> role = user.getRole().stream().map(Role::getName).collect(Collectors.toList());
			if(role.contains(AuthoritiesConstants.SUPER_ADMIN)) {
			packageListToSave = savePackageRequestList.stream().map(item -> {
				if(packageRepository.findByPackageName(item.getPackageName())!=null) {
					logger.info("Exception Occured While Saving PAckage.");
					throw new BadRequestException("Package name already exist " + item.getPackageName());
				}
				Package package1  = beanConverter.packageRequestToPackage(item, true, user);
//				List<Package> identical = isIdenticalPackage(package1);
//				 if( identical != null && identical.size() > 0) {
//					 	List<String> packageNames = identical.stream().map(x -> x.getPackageName()).collect(Collectors.toList());
//					 	String delim = ", ";
//					 	String res = String.join(delim, packageNames);
//						logger.info("Exception Occured While Saving PAckage.");
//						throw new BadRequestException("Identical package already exist " + res);
//					}
				return package1;
			}).collect(Collectors.toList());
			if (packageListToSave.size() > 0) {
				packageRepository.saveAll(packageListToSave);
			}
		}else {
			throw new DeviceVersionException("Super Admin usernot found");
		}
		} else {
			throw new DeviceVersionException("No user found for userId " + userName);
		}
	}
    
    private List<Package> isIdenticalPackage(Package package1)
    {
		return packageRepository.findIdentical(package1.getAppVersion().trim().toLowerCase(), package1.getBinVersion().trim().toLowerCase(),
				package1.getBleVersion().trim().toLowerCase(), package1.getMcuVersion().trim().toLowerCase(), package1.getConfig1().trim().toLowerCase(), package1.getConfig2().trim().toLowerCase(),
				package1.getConfig3().trim().toLowerCase(), package1.getConfig4().trim().toLowerCase(), package1.getConfig1Crc().trim().toLowerCase(), package1.getConfig2Crc().trim().toLowerCase(),
				package1.getConfig3Crc().trim().toLowerCase(), package1.getConfig4Crc().trim().toLowerCase(), package1.getDeviceType().trim().toLowerCase(),
				package1.getLiteSentryHardware().trim().toLowerCase(), package1.getLiteSentryApp().trim().toLowerCase(), package1.getLiteSentryBoot().trim().toLowerCase(),
				package1.getMicrospMcu().trim().toLowerCase(), package1.getMicrospApp().trim().toLowerCase(), package1.getCargoMaxbotixHardware().trim().toLowerCase(),
				package1.getCargoMaxbotixFirmware().trim().toLowerCase(), package1.getCargoRiotHardware().trim().toLowerCase(), package1.getCargoRiotFirmware().trim().toLowerCase());
    }
    
    @Override
	public Boolean getPackageByName(String packageName) {

    	  Package packageData = packageRepository.findByPackageName(packageName);
          if (packageData != null) {
			return true;
          }
		return false;
	}

	@Override
	public Page<PackagePayload> getAllPackage(Map<String, String> filterValues , Pageable pageable, boolean sortByInUse, String order, String userName) {
		User user = restUtils.getUserFromAuthService(userName);
		 List<String> role = user.getRole().stream().map(Role::getName).collect(Collectors.toList());
        Specification<Package> spc = CampaignSpecification.getPackageSpecification(filterValues);
        List<PackagePayload> packageResponseList = new ArrayList<>();
        List<Package> packageLists = new ArrayList<>();
		Page<Package> packageList =  new PageImpl<>(packageLists);;
		if(role.contains(AuthoritiesConstants.SUPER_ADMIN)) {
		packageList = packageRepository.findAll(spc, pageable);
		packageList.forEach(packageData -> {
				packageResponseList.add(beanConverter.packageToPackageResponse(packageData));
		});
		if (sortByInUse) {
		packageResponseList.sort(Comparator.comparing(PackagePayload::getIsUsedInCampaign, (star1, star2) -> {
				    if(star1 == star2){
				         return 0;
				    }
				    if (order.equalsIgnoreCase(Constants.ASCENDING_ORDER)) {
				    	return star1 ? -1 : 1;
					}
				    return star1 ? 1 : -1;
				}));
		}
		}
		Page<PackagePayload> page = new PageImpl<>(packageResponseList, packageList.getPageable(), packageList.getTotalElements());
		return page;
	}
	

	@Override
	@Transactional
	public void deleteById(String uuid) {
		Package packageData = packageRepository.findByUuid(uuid);
		if (packageData == null) {
			throw new BadRequestException("package not found for id");
		}
		Long inUseCamp = campaignStepRepository.findPackageUsedInCampaign(packageData.getUuid());
		if (inUseCamp > 0) {
			throw new DeviceVersionException("Package is in use");
		}
		packageRepository.deleteByUuid(uuid);
	}

	@Override
	public PackagePayload getByUuid(String packageUuid) {
		Package packageData = packageRepository.findByUuid(packageUuid);
		if (packageData == null) {
			throw new DeviceVersionException("package not found for id");
		}
		PackagePayload packageResponse = beanConverter.packageToPackageResponse(packageData);
		return packageResponse;
	}

	@Override
	public void update(PackagePayload packageToUpdate, String userName) {

			if (packageToUpdate.getUuid() == null) {
				throw new BadRequestException("Invalid Request");
			}
			Package packageData = packageRepository.findByUuid(packageToUpdate.getUuid());
			User user = restUtils.getUserFromAuthService(userName);
			if (packageData == null) {
				throw new DeviceVersionException("No Package found, Invalid Request");
			}
			Package updatedPackage = beanConverter.updatePackageToPackage(packageToUpdate , packageData , user);
			List<Package> identical = isIdenticalPackageWhileUpdate(updatedPackage);
			 if( identical != null && identical.size() > 0) {
				 	List<String> packageNames = identical.stream().map(x -> x.getPackageName()).collect(Collectors.toList());
				 	String delim = ", ";
				 	String res = String.join(delim, packageNames);
					logger.info("Exception Occured While Saving PAckage.");
					throw new BadRequestException("Identical package already exist " + res);
				}
			packageRepository.save(updatedPackage);
	}
	
	 private List<Package> isIdenticalPackageWhileUpdate(Package package1)
	    {
			return packageRepository.findIdentical(package1.getAppVersion().trim().toLowerCase(), package1.getBinVersion().trim().toLowerCase(),
					package1.getBleVersion().trim().toLowerCase(), package1.getMcuVersion().trim().toLowerCase(), package1.getConfig1().trim().toLowerCase(), package1.getConfig2().trim().toLowerCase(),
					package1.getConfig3().trim().toLowerCase(), package1.getConfig4().trim().toLowerCase(), package1.getConfig1Crc().trim().toLowerCase(), package1.getConfig2Crc().trim().toLowerCase(),
					package1.getConfig3Crc().trim().toLowerCase(), package1.getConfig4Crc().trim().toLowerCase(), package1.getDeviceType().trim().toLowerCase(),
					package1.getLiteSentryHardware().trim().toLowerCase(), package1.getLiteSentryApp().trim().toLowerCase(), package1.getLiteSentryBoot().trim().toLowerCase(),
					package1.getMicrospMcu().trim().toLowerCase(), package1.getMicrospApp().trim().toLowerCase(), package1.getCargoMaxbotixHardware().trim().toLowerCase(),
					package1.getCargoMaxbotixFirmware().trim().toLowerCase(), package1.getCargoRiotHardware().trim().toLowerCase(), package1.getCargoRiotFirmware().trim().toLowerCase(),
					package1.getPackageId());
	    }

	// Old repository method call
	/*
	@Override
	public List<Package> fetchALLPackagesForCsv(HttpServletResponse response) throws IOException {
		logger.info("Inside fetchALLPackagesForCsv Method IN Service Impl Layer.");
		restUtils.writeCSVFile(Constants.CSVFILE, packageRepository.findAll(),response);
		return packageRepository.findAll();
	}*/

	@Override
	public List<Package> fetchALLPackagesForCsv(HttpServletResponse response) throws IOException {
		logger.info("Inside fetchALLPackagesForCsv Method IN Service Impl Layer.");
		restUtils.writeCSVFile(Constants.CSVFILE, packageRepository.findByActiveFalse(), response);
		return packageRepository.findByActiveFalse();
	}

	@Override
	public void fetchFilterdPackagesForCsv(List<PackagePayload> payloadList, HttpServletResponse response) throws IOException {
		logger.info("Inside fetchFilterdPackagesForCsv Method IN Service Impl Layer.");
		restUtils.writeFilterCSVFile(Constants.CSVFILE_FILTERED, payloadList,response);
	}

	@Override
	public Boolean getEmergencyStopFlagValue() {
		Boolean emergencyStopFlag = false;
		List<EmergencyStop> listOfemergencyStop = emergencyStopRepository.findAll();
		if(listOfemergencyStop != null && !listOfemergencyStop.isEmpty()) {
			emergencyStopFlag = listOfemergencyStop.get(0).getIsEmergencyStop();
		}
		return emergencyStopFlag;
	}

	@Override
	public Boolean updateEmergencyStopFlagValue(Boolean isEmergencyStop) {
		Boolean emergencyStopFlag = false;
		EmergencyStop emergencyStop = null;
		if(isEmergencyStop != null) {
			List<EmergencyStop> listOfemergencyStop = emergencyStopRepository.findAll();
			if(listOfemergencyStop != null && !listOfemergencyStop.isEmpty()) {
				emergencyStop = listOfemergencyStop.get(0);
				emergencyStop.setIsEmergencyStop(isEmergencyStop);
			} else {
				emergencyStop = new EmergencyStop();
				emergencyStop.setIsEmergencyStop(isEmergencyStop);
			}
			emergencyStopRepository.save(emergencyStop);
			emergencyStopFlag = true;
		}
		return emergencyStopFlag;
	}


}
