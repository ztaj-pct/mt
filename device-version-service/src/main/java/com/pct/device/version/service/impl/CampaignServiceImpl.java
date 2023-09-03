package com.pct.device.version.service.impl;

import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.persistence.Tuple;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.pct.common.dto.MsDeviceRestResponse;
//import com.pct.common.model.Company;
import com.pct.common.model.Role;
import com.pct.common.model.User;
import com.pct.common.util.JwtUser;
import com.pct.device.service.device.CampaignInstalledDevice;
import com.pct.device.service.device.DeviceReport;
import com.pct.device.version.constant.CampaignListDisplayStatus;
import com.pct.device.version.constant.CampaignStatus;
import com.pct.device.version.constant.CampaignStepDeviceStatus;
import com.pct.device.version.constant.DeviceStatusForCampaign;
import com.pct.device.version.constant.GroupingConstants;
import com.pct.device.version.dto.StepDTO;
import com.pct.device.version.dto.VersionMigrationDetailDTO;
import com.pct.device.version.exception.BadRequestException;
import com.pct.device.version.exception.DeviceInMultipleCampaignsException;
import com.pct.device.version.exception.DeviceVersionBatchNotificationException;
import com.pct.device.version.exception.DeviceVersionException;
import com.pct.device.version.model.Campaign;
import com.pct.device.version.model.CampaignListDisplay;
import com.pct.device.version.model.CampaignStatsPayloadList;
import com.pct.device.version.model.CampaignStep;
import com.pct.device.version.model.CampaignStepDeviceDetail;
import com.pct.device.version.model.Grouping;
import com.pct.device.version.model.MultipleCampaignDevice;
import com.pct.device.version.model.Package;
import com.pct.device.version.payload.CampaignDeviceDetail;
import com.pct.device.version.payload.CampaignHistory;
import com.pct.device.version.payload.CampaignPayload;
import com.pct.device.version.payload.CampaignStatsPayload;
import com.pct.device.version.payload.CampaignSummary;
import com.pct.device.version.payload.CurrentCampaignResponse;
import com.pct.device.version.payload.DeviceCampaignHistory;
import com.pct.device.version.payload.DeviceStepStatus;
import com.pct.device.version.payload.DeviceWithEligibility;
import com.pct.device.version.payload.ExecuteCampaignRequest;
import com.pct.device.version.payload.PackagePayload;
import com.pct.device.version.payload.PackageSequence;
import com.pct.device.version.payload.SaveCampaignRequest;
import com.pct.device.version.payload.SelectedDevice;
import com.pct.device.version.payload.UpdateCampaignPayload;
import com.pct.device.version.repository.ICampaignListDisplayRepository;
import com.pct.device.version.repository.ICampaignRepository;
import com.pct.device.version.repository.ICampaignStepDeviceDetailRepository;
import com.pct.device.version.repository.ICampaignStepRepository;
import com.pct.device.version.repository.IDeviceVersionRepository;
import com.pct.device.version.repository.IMultipleCampaignDeviceRepository;
import com.pct.device.version.repository.IPackageRepository;
//import com.pct.device.version.repository.msdevice.ICampaignInstalledDeviceMsRepository;
import com.pct.device.version.repository.projections.CampaignIdAndNameView;
import com.pct.device.version.repository.projections.StepStatusForDeviceView;
import com.pct.device.version.service.ICampaignService;
import com.pct.device.version.service.IGroupingService;
import com.pct.device.version.specification.CampaignSpecification;
import com.pct.device.version.util.BeanConverter;
import com.pct.device.version.util.CampaignDeviceHelper;
import com.pct.device.version.util.Constants;
import com.pct.device.version.util.DeviceDetailProcessor;
import com.pct.device.version.util.RestUtils;
import com.pct.device.version.validation.AuthoritiesConstants;
import com.pct.device.version.validation.CampaignUtils;

@Service
public class CampaignServiceImpl implements ICampaignService {

	Logger logger = LoggerFactory.getLogger(CampaignServiceImpl.class);
	Logger analysisLog = LoggerFactory.getLogger("analytics");

	@Autowired
	private BeanConverter beanConverter;
	@Autowired
	private RestUtils restUtils;
	@Autowired
	private ICampaignRepository campaignRepo;
	@Autowired
	private ICampaignStepRepository campaignStepRepository;
	@Autowired
	private IMultipleCampaignDeviceRepository multipleCampaignDeviceRepository;
	@Autowired
	private IGroupingService groupingService;
	@Autowired
	private CampaignUtils campaignUtils;
	@Autowired
	private IPackageRepository packageRepo;
	@Autowired
	private CampaignDeviceHelper campaignDeviceHelper;
	@Autowired
	private ICampaignStepDeviceDetailRepository deviceDetailRepository;
	/*@Autowired
	private ICampaignInstalledDeviceMsRepository iCampaignInstalledDeviceMsRepository;
*/
	@Autowired
	private ICampaignListDisplayRepository campListRepo;

	@Autowired
	private ICampaignStepDeviceDetailRepository stepDeviceDetailRepository;
	@Autowired
	private IPackageRepository packageRepository;
	@Autowired
	private DeviceDetailProcessor deviceDetailProcessor;

	@Autowired
	private IMultipleCampaignDeviceRepository multiCampaignDeviceRepository;
	@Autowired
	private IDeviceVersionRepository deviceVersionRepository;

	@Override
	@Transactional
	public String saveCampaign(SaveCampaignRequest saveCampaignRequest, Long userId) {
		String msgUuid = UUID.randomUUID().toString();
		logger.info("Save Campaign called");
		Campaign campaignToSave;
		List<CampaignStep> campaignStepsToSave;
		Campaign savedCamp;
		JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		User user = restUtils.getUserFromAuthService(jwtUser.getUsername());

		if (user != null) {
			List<String> role = user.getRole().stream().map(Role::getName).collect(Collectors.toList());
			if (role.contains(AuthoritiesConstants.SUPER_ADMIN)) {

				if (StringUtils.isEmpty(saveCampaignRequest.getImeiList())
						&& StringUtils.isEmpty(saveCampaignRequest.getCustomerName())) {
					throw new BadRequestException("Invalid Request - target imeis not selected");
				}
				Grouping grouping = groupingService.saveTargetImei(saveCampaignRequest.getImeiList(),
						saveCampaignRequest.getCustomerName(), saveCampaignRequest.getNotEligibleDevices(),
						saveCampaignRequest.getExcludedImeis());

				campaignToSave = beanConverter.campaignRequestToCampaign(grouping, saveCampaignRequest, true, user);
				savedCamp = campaignRepo.save(campaignToSave);
				logger.info("Campaign saved: ", savedCamp.getCampaignName());
				campaignStepsToSave = beanConverter.campaignRequestToCampaignStep(saveCampaignRequest, savedCamp, true,
						user);
				campaignStepRepository.saveAll(campaignStepsToSave);
				logger.info("Campaign steps saved:  " + campaignStepsToSave.size());

				try {
					createCampaignListViewForCampaign(savedCamp);
					deviceDetailProcessor.processDevicesForAddedCampaign(campaignToSave, campaignStepsToSave, msgUuid);
				} catch (Exception e) {
					logger.error("Exception while saving Campaign List View " + e.getMessage());
				}
				return campaignToSave.getUuid();
			} else {
				logger.error("Exception while saving Campaign ,  SuperAdmin user found ");
				throw new DeviceVersionException("User can not create campaign");
			}
		} else {
			logger.error("Exception while saving Campaign , No user found ");
			throw new DeviceVersionException("No user found for userId " + userId);
		}

	}

	private void createCampaignListViewForCampaign(Campaign campaign) throws Exception {

		CampaignListDisplay campaignListDisplay = new CampaignListDisplay();
		campaignListDisplay.setDaysRunning(CampaignListDisplayStatus.PENDING.getValue());
		campaignListDisplay.setLastCommandDays(CampaignListDisplayStatus.PENDING.getValue());
		campaignListDisplay.setUuid(campaign.getUuid());
		campaignListDisplay.setCampaignListItemId(campaign.getCampaignId());
		campaignListDisplay.setCampaignName(campaign.getCampaignName());
		campaignListDisplay.setDescription(campaign.getDescription());
		campaignListDisplay.setCampaignStatus(campaign.getCampaignStatus().getValue());
		campaignListDisplay.setCreatedAt(campaign.getCreatedAt());
		campaignListDisplay.setCompleted(-100l);
		campaignListDisplay.setEligible(-100l);
		campaignListDisplay.setProblemCount(-100l);
		campaignListDisplay.setInProgress(-100l);
		campaignListDisplay.setNotStarted(-100l);
		campaignListDisplay.setDeviceType(campaign.getDeviceType());

		campaignListDisplay.setCampaignStartDate(campaign.getCampaignStartDate());
		if (campaign.getCreatedBy() != null) {
			campaignListDisplay
					.setCreatedBy(campaign.getCreatedBy().getFirstName() + " " + campaign.getCreatedBy().getLastName());
		}
		campaignListDisplay.setListUpdatedAt(OffsetDateTime.now( ZoneId.of( "UTC" ) ).toInstant().toString());
		campListRepo.save(campaignListDisplay);
	}

	@Override
	public Page<CampaignPayload> getAllCampaign(Map<String, String> filterValues, Pageable pageable) {
		checkForPausedCampaigns();
		checkForCompletedCampaigns();
		Specification<CampaignStep> spc = CampaignSpecification.getCampaignSpecification(filterValues);
		Page<CampaignStep> campaignList = campaignStepRepository.findAll(spc, pageable);

		Map<Campaign, List<CampaignStep>> campaignListGrouped = campaignList.stream()
				.collect(Collectors.groupingBy(w -> w.getCampaign()));
		List<CampaignPayload> campaignResponseList = new ArrayList<>();

//		campaignlistGrouped.forEach((k,v)-> campaignResponseList.add(beanConverter.campaignToCampaignResponse(k,v)));

		for (CampaignStep finalStep : campaignList) {
			Campaign temp = finalStep.getCampaign();
			if (campaignListGrouped.get(temp) != null) {
				campaignResponseList.add(beanConverter.campaignToCampaignResponse(temp, campaignListGrouped.get(temp)));
				campaignListGrouped.remove(temp);
			}
		}

		Page<CampaignPayload> page = new PageImpl<>(campaignResponseList, campaignList.getPageable(),
				campaignResponseList.size());

		return page;
	}

	@Override
	public Page<CampaignListDisplay> findALLCampaignList(Map<String, String> filterValues, Pageable pageable,
			String userName) {
		User user = restUtils.getUserFromAuthService(userName);
		logger.info("Inside findALLCampaignList service level");
		Specification<CampaignListDisplay> spc = CampaignSpecification.getCSPLSpecification(filterValues);
		List<String> role = user.getRole().stream().map(Role::getName).collect(Collectors.toList());
		List<CampaignListDisplay> campaignListDisplay = new ArrayList<>();
		Page<CampaignListDisplay> list = new PageImpl<>(campaignListDisplay);
		;
		if (role.contains(AuthoritiesConstants.SUPER_ADMIN)) {
			list = campListRepo.findAll(spc, pageable);
		}
		logger.info("Fetched CampaignListDisplay " + list.getSize());
		Page<CampaignListDisplay> page = new PageImpl<>(list.getContent(), list.getPageable(), list.getTotalElements());
		return page;
	}

	@Override
	public CampaignStatsPayload getByUuid(String campaignUuid, String msgUuid, int method) {
		logger.info(msgUuid + " . Inside getByUuid serv meth.");
		Campaign campaignData = campaignRepo.findByUuid(campaignUuid);
		campaignData = campaignUtils.checkForPausedCampaign(campaignData);
		campaignData = campaignUtils.checkForCompletedCampaign(campaignData);
		if (campaignData == null) {
			throw new DeviceVersionException("Campaign not found for id");
		}
		List<CampaignStep> allCampaignSteps = campaignStepRepository.findByCampaignUuid(campaignData.getUuid());
		logger.info(msgUuid + " . calling beanConverter.campaignToCampaignStatsResponse");
		CampaignStatsPayload campaignResponse = null;
		if (method == 0)
			campaignResponse = beanConverter.campaignToCampaignStatsResponse(campaignData, allCampaignSteps, msgUuid);
		if (method == 1)
			campaignResponse = beanConverter.campaignToCampaignStatsResponseNew(campaignData, allCampaignSteps,
					msgUuid);
		if (method == 2) {
			deviceDetailProcessor.processDevicesForUpdateCampaign(campaignData, allCampaignSteps, msgUuid);
			campaignResponse = beanConverter.campaignToCampaignStatsResponseNew(campaignData, allCampaignSteps,
					msgUuid);
		}

		logger.info(msgUuid + " . returning response");
		return campaignResponse;
	}

	@Override
	@Transactional
	public void deleteById(String uuid) {
		Campaign campaignData = campaignRepo.findByUuid(uuid);
		if (campaignData == null) {
			throw new BadRequestException("Campaign not found for id");
		}
		campaignRepo.deleteByUuid(uuid);
		campListRepo.deleteByUuid(uuid);
	}

	@Override
	@Transactional
	public String update(UpdateCampaignPayload campaignToUpdate, String userName) {
		List<String> toReturn = null;
		User user = restUtils.getUserFromAuthService(userName);
		String uuid = UUID.randomUUID().toString() + " ";
		logger.info(uuid + " Update Request Recieved " ); 
		if (campaignToUpdate.getUuid() == null) {
			throw new BadRequestException("Invalid Request");
		}

		Campaign campaignData = campaignRepo.findByUuid(campaignToUpdate.getUuid());
		if (campaignData == null) {
			throw new DeviceVersionException(uuid + "Campaign not found for UUID");
		}
		
		logger.info(uuid + " Update Request Recieved  for "+ campaignData.getCampaignName() + " " +campaignData.getUuid()   );
		/*
		 * if (StringUtils.isEmpty(campaignToUpdate.getImeiList()) &&
		 * StringUtils.isEmpty(campaignToUpdate.getCustomerName())) { throw new
		 * BadRequestException("Invalid Request - target imeis not selected"); }
		 */

		if (campaignToUpdate.getDeviceStatusForCampaign() != null
				&& campaignToUpdate.getDeviceStatusForCampaign().equals("Eligible")) {
		/*	List<String> imeisToBeRemoved = campaignToUpdate.getImeisToBeRemoved();
			for (String imei : imeisToBeRemoved) {
				CampaignDeviceDetail campaignDeviceDetail = campaignToUpdate.getCampaignDeviceDetail().stream()
						.filter(m -> m.getImei().equals(imei)).findAny().get();
				if (campaignDeviceDetail != null) {
					Optional<DeviceStepStatus> deviceStepStatus = campaignDeviceDetail.getDeviceStepStatus().stream()
							.filter(m -> m.getStepStatus().equals("In progress")).findAny();

					if (deviceStepStatus.isPresent()) {
						CampaignStep campaignStep = campaignStepRepository.findByCampaignUuidAndStepOrderNumber(
								campaignToUpdate.getUuid(), deviceStepStatus.get().getStepOrderNumber());
						List<CampaignStepDeviceDetail> campaignStepDeviceDetailList = stepDeviceDetailRepository
								.findByDeviceIdAndCampaignUuidAndCampaignStepUuid(imei, campaignToUpdate.getUuid(),
										campaignStep.getUuid());
						if (campaignStepDeviceDetailList.size() > 0) {
							CampaignStepDeviceDetail campaignStepDeviceDetail = campaignStepDeviceDetailList.get(0);
							campaignStepDeviceDetail.setStatus(CampaignStepDeviceStatus.REMOVED);
							campaignStepDeviceDetail.setUpdatedAt(Instant.now());
							campaignStepDeviceDetail.setUpdatedBy(user);
							stepDeviceDetailRepository.save(campaignStepDeviceDetail);
						} else {
							CampaignStepDeviceDetail campaignStepDeviceDetail = new CampaignStepDeviceDetail();
							campaignStepDeviceDetail.setCampaign(campaignData);
							campaignStepDeviceDetail.setCampaignStep(campaignStep);
							campaignStepDeviceDetail.setDeviceId(imei);
							String stepDeviceDetailUuid = "";
							boolean isCompanyUuidUnique = false;
							while (!isCompanyUuidUnique) {
								stepDeviceDetailUuid = UUID.randomUUID().toString();
								CampaignStepDeviceDetail byUuid = stepDeviceDetailRepository
										.findByUuid(stepDeviceDetailUuid);
								if (byUuid == null) {
									isCompanyUuidUnique = true;
								}
							}
							campaignStepDeviceDetail.setStatus(CampaignStepDeviceStatus.REMOVED);
							campaignStepDeviceDetail.setUuid(stepDeviceDetailUuid);
							campaignStepDeviceDetail.setUpdatedAt(Instant.now());
							campaignStepDeviceDetail.setUpdatedBy(user);
							stepDeviceDetailRepository.save(campaignStepDeviceDetail);

						}
					} else {
						throw new DeviceVersionException("Step is not In progress state for device: " + imei);

					}
				}

			}*/
		} else {
			if (campaignData.getGroup().getGroupingType().equalsIgnoreCase(GroupingConstants.GROUPING_TYPE_IMEI)) {
				List<String> existingIMEIs = new ArrayList<>(
						Arrays.asList(campaignData.getGroup().getTargetValue().split(",")));
				List<String> toBeUpdatedIMEIs = new ArrayList<>(
						Arrays.asList(campaignToUpdate.getImeiList().split(",")));
				
				logger.info(uuid + " existingIMEIs : "+existingIMEIs);
				logger.info(uuid + " toBeUpdatedIMEIs : "+toBeUpdatedIMEIs);
				existingIMEIs.removeAll(toBeUpdatedIMEIs);
				logger.info(uuid + " existingIMEIs 1: "+existingIMEIs);

				existingIMEIs.forEach(imei -> {
					List<MultipleCampaignDevice> multipleCampaigns = multipleCampaignDeviceRepository
							.findByDeviceId(imei);
					if (multipleCampaigns != null && !multipleCampaigns.isEmpty()) {
						if (multipleCampaigns.size() <= 2) {
							multipleCampaignDeviceRepository.deleteAll(multipleCampaigns);
						} else {
							MultipleCampaignDevice specificEntry = multipleCampaignDeviceRepository
									.findByDeviceIdAndCampaignUuid(imei, campaignData.getUuid());
							multipleCampaignDeviceRepository.delete(specificEntry);
						}
					}
				});
				
				ArrayList<String> existingImeis = new ArrayList<>();
				if(campaignData.getGroup().getTargetValue() != null)
				{
					
					existingImeis = new ArrayList<>(
				
						Arrays.asList(campaignData.getGroup().getTargetValue().split(",")));
					logger.info(uuid + " existingIMEIs 2  "+existingIMEIs);
				}
				
				String imeis = campaignToUpdate.getImeiList();
				logger.info(uuid + " Recieved imes to updated "+imeis);
				
				if(imeis !=null)
				{
					ArrayList<String> newImeis = new ArrayList<>(Arrays.asList(imeis.split(",")));
					logger.info(uuid + " newImeis  2  "+newImeis);

				    toReturn = new ArrayList<String>(newImeis);
						toReturn.removeAll(existingImeis);
						logger.info(uuid + " toReturn  2  "+toReturn);
				}
			 
			}
			Grouping grouping = groupingService.updateTargetImei(campaignToUpdate, campaignData,uuid);
			Campaign updatedCampaign = beanConverter.updateCampaignPayloadToCampaign(grouping, campaignToUpdate,
					campaignData, user);
			updatedCampaign = campaignRepo.save(updatedCampaign);
			CampaignListDisplay campaignListDisplay = campListRepo.findByUuid(updatedCampaign.getUuid());
			if (campaignListDisplay != null) {
				boolean isChanged = false;
				if (!campaignListDisplay.getCampaignName().equals(updatedCampaign.getCampaignName())) {
					campaignListDisplay.setCampaignName(updatedCampaign.getCampaignName());
					isChanged = true;
				}

				if (!campaignListDisplay.getDescription().equals(updatedCampaign.getDescription())) {
					campaignListDisplay.setDescription(updatedCampaign.getDescription());
					isChanged = true;
				}
				if (isChanged) {
					campListRepo.save(campaignListDisplay);
				}
			}
			campaignListDisplay.getCampaignListItemId();

			List<List<CampaignStep>> campaignStepToUpdated = beanConverter
					.updateCampaignPayloadToCampaignStep(campaignToUpdate, updatedCampaign, user);
			campaignStepRepository.deleteInBatch(campaignStepToUpdated.get(0));
			campaignStepRepository.saveAll(campaignStepToUpdated.get(1));
			logger.info(uuid +" toReturn "+toReturn);
			if(toReturn !=null && toReturn.size() >0)
			{
				 deviceDetailProcessor.processDevicesForUpdate(updatedCampaign , campaignStepToUpdated.get(1), uuid ,toReturn);
			}
		}

		return campaignToUpdate.getUuid();
	}

	@Override
	public List<DeviceWithEligibility> getAllDeviceFromMS(String customerName, int page, Integer pageSize, String sort,
			String order, String basePackageUuid, String campaignUuid, String msgUuid) {
		// List<Device> msDevices =
		// restUtils.getDevicesFromMSByCustomerName(customerName);
		Page<MsDeviceRestResponse> msDevices = restUtils.getDeviceDataFromMS(customerName, page, pageSize, sort, order);

		List<DeviceWithEligibility> deviceDataList = new ArrayList<DeviceWithEligibility>();
		DeviceWithEligibility deviceData = null;
		Package baseLine = packageRepo.findByUuid(basePackageUuid);

		List<String> allImeis = msDevices.stream().map(MsDeviceRestResponse::getImei).collect(Collectors.toList());
//		List<DeviceReport> msDeviceReports = restUtils.getLastMaintReportFromMS(AllImei);
		List<DeviceReport> msDeviceReports = campaignDeviceHelper.getDeviceReports(allImeis);
		Map<String, List<DeviceReport>> deviceMaintReportMap = msDeviceReports.stream()
				.collect(Collectors.groupingBy(w -> w.getDEVICE_ID()));

		Long maxStepInCamp = null;

		if (!StringUtils.isEmpty(campaignUuid)) {
			maxStepInCamp = campaignStepRepository.findLastStepByCampaignUuid(campaignUuid).getStepOrderNumber();
		}

		for (MsDeviceRestResponse device : msDevices) {
			deviceData = new DeviceWithEligibility();
			gatewayEligibilityCalculation(device.getImei(), baseLine, deviceData, campaignUuid, maxStepInCamp,
					deviceMaintReportMap, msgUuid);
			String deviceId = device.getImei();
			deviceData.setDEVICE_ID(deviceId);
			deviceData.setOWNER_LEVEL_2(device.getOrganisationName());
			deviceData.setDEVICE_MODEL(device.getProductName());
			deviceData.setOrganisationName(device.getOrganisationName());
			deviceData.setImei(deviceId);
			deviceDataList.add(deviceData);

		}

		return deviceDataList;

	}

	// Returns check digit for 14 digit IMEI prefix
	public int getCheckDigit(String imeiPrefix) {
		int sum = 0;
		for (int i = 13; i >= 0; i = i - 1) {
			String sDigit = imeiPrefix.substring(i, i + 1);
			int digit = Integer.valueOf(sDigit);
			if (i % 2 == 0) {
				sum = sum + digit;
			} else {
				sum = sum + sumOfDigits(digit * 2);
			}
		}
		sum = sum * 9;
		return sum % 10; // Return check digit
	}

	// Calculate sum of digits for a number
	public int sumOfDigits(int number) {
		int sum = 0;
		while (number > 0) {
			sum += number % 10;
			number = number / 10;
		}
		return sum;
	}

	public Boolean checkImeiByLuhnAlgo(String imei) {
		Boolean isImei = false;

		if (imei != null && imei != "" && imei.length() == 15) {
			int computedCheckDigit = getCheckDigit(imei.substring(0, 14));
			int checkDigitInSource = Integer.valueOf(imei.substring(14));

			if (computedCheckDigit == checkDigitInSource) {
				isImei = true;
				System.out.println(imei + " is a valid IMEI number!");
			} else {
				System.out.println(imei + " is NOT a valid IMEI number!");
				System.out.println("Check digit computed: " + computedCheckDigit);
			}
		}
		return isImei;
	}

	@Override
	public SelectedDevice getSelectedDevices(List<String> imeiList, int i, Integer pageSize, String sort, String order,
			String basePackageUuid, String campaignUuid, String msgUuid) {
		logger.info(msgUuid + "getSelectedDevices basePackageUuid" + basePackageUuid);
		List<String> invalidListOfImei = new ArrayList<String>();
		List<String> validListOfImei = new ArrayList<String>();
		for (String imei : imeiList) {
			if (imei.length() < 15) {
				String updatedImei = "0" + imei;
				if (checkImeiByLuhnAlgo(updatedImei)) {
					validListOfImei.add(updatedImei);
				} else {
					invalidListOfImei.add(updatedImei);
				}

			} else {
				validListOfImei.add(imei);
			}
		}

		if (invalidListOfImei != null && invalidListOfImei.size() > 0) {
			throw new DeviceVersionBatchNotificationException(
					"The following IMEI values are invalid. IMEIs must be numeric and 15 characters long",
					invalidListOfImei);
		}
	

		/*
		 * List<String> collect = imeiList.stream().filter(a -> a.length() <
		 * 15).collect(Collectors.toList()); if (collect != null && collect.size() > 0)
		 * { throw new
		 * DeviceVersionBatchNotificationException("The following IMEI values are invalid. IMEIs must be numeric and 15 characters long"
		 * ,collect); }
		 */
		//List<MsDeviceRestResponse> msDevices = restUtils.getSelectedDevicesFromMSByImeis(validListOfImei);
		List<MsDeviceRestResponse> msDevices = deviceVersionRepository.findAllDeviceByIMEIList(validListOfImei);
		logger.info(msgUuid + "getSelectedDevices msDevices" + msDevices.size());
		List<DeviceWithEligibility> deviceDataList = new ArrayList<DeviceWithEligibility>();
		DeviceWithEligibility deviceData = null;
		Package baseLine = packageRepo.findByUuid(basePackageUuid);
		logger.info(msgUuid + "getSelectedDevices basePackageUuid" );
		List<String> allImei = msDevices.stream().map(MsDeviceRestResponse::getImei).collect(Collectors.toList());
		validListOfImei.removeAll(allImei);
		if (validListOfImei != null && validListOfImei.size() > 0) {
			throw new DeviceVersionBatchNotificationException(
					"The following IMEI values were not found on the maintenance server and will not be added to the list for this campaign.",
					validListOfImei);
		}
		// List<DeviceReport> msDeviceReports =
		// restUtils.getLastMaintReportFromMS(allImei);
	//	List<DeviceReport> msDeviceReports = campaignDeviceHelper.getDeviceReports(allImei);
		//logger.info(msgUuid + "msDeviceReports "+msDeviceReports.size() );
	//	Map<String, String> msDeviceLastReportDate = campaignDeviceHelper.getDeviceLastReportDate(allImei);
		logger.info(msgUuid + "msDeviceLastReportDate " );
		//Map<String, List<DeviceReport>> deviceMaintReportMap = msDeviceReports.stream()
		//		.collect(Collectors.groupingBy(w -> w.getDEVICE_ID()));

		Long maxStepInCamp = null;

		if (!StringUtils.isEmpty(campaignUuid)) {
			maxStepInCamp = campaignStepRepository.findLastStepByCampaignUuid(campaignUuid).getStepOrderNumber();
		}

		for (MsDeviceRestResponse device : msDevices) {
			
			deviceData = new DeviceWithEligibility();
			String deviceId = device.getImei();
			logger.info(msgUuid + " getSelectedDevices  "+ deviceId);
		//	gatewayEligibilityCalculation(deviceId, baseLine, deviceData, campaignUuid, maxStepInCamp,
			//		deviceMaintReportMap, msgUuid);
			logger.info(msgUuid + " getSelectedDevices  gatewayEligibilityCalculation "+ deviceId);
			//deviceData.setLast_report(msDeviceLastReportDate.get(deviceId));
			deviceData.setDEVICE_ID(deviceId);
			// deviceData.setOWNER_LEVEL_2(device.getOwnerLevel2());
			deviceData.setOWNER_LEVEL_2(device.getOrganisationName());

			deviceData.setDEVICE_MODEL(device.getProductName());
			// deviceData.setOrganisationName(device.getOrganisationName());
			deviceData.setOrganisationName(device.getOrganisationName());
			deviceData.setImei(device.getImei());
			String deivceInCampaigns = deviceData.getDeviceInCampaigns();
			if (!StringUtils.isEmpty(deivceInCampaigns)) {
				invalidListOfImei.add(deviceId);
			}
			deviceDataList.add(deviceData);
			logger.info(msgUuid + " getSelectedDevices  processing complete  for"+ deviceId);
		}

		SelectedDevice selectedDevice = new SelectedDevice();
		selectedDevice.setDeviceWithEligibilityList(deviceDataList);
		selectedDevice.setInvalidImeiList(invalidListOfImei);
		logger.info(msgUuid + "getSelectedDevices Completed"  );
		return selectedDevice;

	}

	private void checkForCompletedCampaigns() {
		List<Campaign> activeCampaigns = campaignRepo.findByStatus(CampaignStatus.IN_PROGRESS);
		activeCampaigns.forEach(activeCampaign -> {
			campaignUtils.checkForCompletedCampaign(activeCampaign);
		});
	}

	private void checkForPausedCampaigns() {
		List<Campaign> activeCampaigns = campaignRepo.findByStatus(CampaignStatus.IN_PROGRESS);
		activeCampaigns.forEach(activeCampaign -> {
			campaignUtils.checkForPausedCampaign(activeCampaign);
		});
	}

	@Override
	public List<CampaignIdAndNameView> getBaselinePackageFromExistingCampaign(String packageUuid) {
		List<CampaignIdAndNameView> existingCampaigns = null;
		existingCampaigns = campaignStepRepository.getBaselinePackageFromExistingCampaign(packageUuid);
		return existingCampaigns;
	}

	@Override
	public Boolean getCampaignByName(String campaignName) {

		Campaign campaignData = campaignRepo.findByCampaignName(campaignName.trim());
		if (campaignData != null) {
			return true;
		}
		return false;
	}

	public CampaignStatsPayloadList getByUuid(String campaignUuid, CampaignStatsPayloadList campaignStatsPayloadList,
			String msgUuid) {
		Campaign campaignData = campaignRepo.findByUuid(campaignUuid);
		if (campaignData == null) {
			throw new DeviceVersionException("Campaign not found for id");
		}

		List<CampaignStep> allCampaignSteps = campaignStepRepository.findByCampaignUuid(campaignData.getUuid());
		campaignStatsPayloadList = beanConverter.campaignToCampaignStatsResponseScheduler(campaignData, allCampaignSteps,
				campaignStatsPayloadList, msgUuid);
		return campaignStatsPayloadList;
	}

	void gatewayEligibilityCalculation(String deviceId, Package baseLine, DeviceWithEligibility deviceData,
			String campaignUuid, Long maxStepInCamp, Map<String, List<DeviceReport>> deviceMaintReportMap,
			String msgUuid) {

		boolean startedInAnyOtherCamp = false;
		String multiCamp = "";
		try {
			List<Campaign> activeCampaignsForImei = campaignUtils.checkCampaignForDeviceId(deviceId, "");
			String deviceInCampaigns = "";
			String isEligible = campaignUtils.checkDeviceMatchesBaseline(deviceId, deviceMaintReportMap, baseLine,
					msgUuid);
			logger.info(msgUuid + " isEligible  "+isEligible +" "+ deviceId);
			if (!isEligible.equalsIgnoreCase(DeviceStatusForCampaign.ELIGIBLE.getValue())) {
				for (Campaign campaign : activeCampaignsForImei) {
					deviceInCampaigns += campaign.getCampaignName() + ",";
				}
			}
			deviceData.setDeviceInCampaigns(deviceInCampaigns);
			if (deviceMaintReportMap.get(deviceId) == null || deviceMaintReportMap.get(deviceId).size() == 0) {
				logger.info("Gateway has not reported recently ");
				deviceData.setDevice_status_for_campaign(DeviceStatusForCampaign.UNKNOWN.getValue());
				deviceData.setComments(Constants.NOT_REPORTED_RECENTLY);
			} else {
				String firstStepStatusInThisCamp = null;
				String lastStepStatusInThisCamp = null;
				if (!StringUtils.isEmpty(campaignUuid)) {
					firstStepStatusInThisCamp = deviceDetailRepository
							.findStatusByCampaignUuidAndStepOrderNumber(campaignUuid, deviceId, 1l);
					lastStepStatusInThisCamp = deviceDetailRepository
							.findStatusByCampaignUuidAndStepOrderNumber(campaignUuid, deviceId, maxStepInCamp);
				}
				logger.info(msgUuid + " firstStepStatusInThisCamp  "+firstStepStatusInThisCamp +" "+ deviceId);
				if (activeCampaignsForImei.size() == 1) {
					if (lastStepStatusInThisCamp != null
							&& lastStepStatusInThisCamp.equalsIgnoreCase(CampaignStepDeviceStatus.SUCCESS.getValue())) {
						deviceData.setDevice_status_for_campaign(DeviceStatusForCampaign.COMPLETED.getValue());
					} else {
						if (isEligible.equalsIgnoreCase(DeviceStatusForCampaign.ELIGIBLE.getValue())
								|| !StringUtils.isEmpty(firstStepStatusInThisCamp)
										&& (lastStepStatusInThisCamp == null || !lastStepStatusInThisCamp
												.equalsIgnoreCase(CampaignStepDeviceStatus.SUCCESS.getValue()))) {

							deviceData.setDevice_status_for_campaign(DeviceStatusForCampaign.ELIGIBLE.getValue());
							if (!StringUtils.isEmpty(campaignUuid)) {
								List<String> failedStepUuid = deviceDetailRepository.getFailedExecutedStep(campaignUuid,
										deviceId);
								logger.info(msgUuid + " failedStepUuid  "+failedStepUuid +" "+ deviceId);
								if (failedStepUuid != null && failedStepUuid.size() > 0) {
									String stepStatus = deviceDetailRepository.getStatusOfStep(failedStepUuid.get(0),
											deviceId);
									if (stepStatus.equalsIgnoreCase(CampaignStepDeviceStatus.PENDING.getValue())) {
										CampaignStep step = campaignStepRepository.findByUuid(failedStepUuid.get(0));
										deviceData.setComments(step.getFromPackage().getPackageName() + " to "
												+ step.getToPackage().getPackageName() + " "
												+ Constants.GATEWAY_PROBLEM_STATUS);
										deviceData.setDevice_status_for_campaign(
												DeviceStatusForCampaign.PROBLEM.getValue());
									}
								}
							}
						} else {
							deviceData.setDevice_status_for_campaign(DeviceStatusForCampaign.NOT_ELIGIBLE.getValue());
							deviceData.setComments(Constants.NOT_ELIGIBLE_FOR_BASELINE);
						}
					}
				} else {
					if (lastStepStatusInThisCamp != null
							&& lastStepStatusInThisCamp.equalsIgnoreCase(CampaignStepDeviceStatus.SUCCESS.getValue())) {
						deviceData.setDevice_status_for_campaign(DeviceStatusForCampaign.COMPLETED.getValue());
					} else {
						deviceData.setDevice_status_for_campaign(DeviceStatusForCampaign.ELIGIBLE.getValue());

						if (isEligible.equalsIgnoreCase(DeviceStatusForCampaign.ELIGIBLE.getValue())
								|| (!StringUtils.isEmpty(firstStepStatusInThisCamp)
										&& (lastStepStatusInThisCamp == null || !lastStepStatusInThisCamp
												.equalsIgnoreCase(CampaignStepDeviceStatus.SUCCESS.getValue())))) {

							for (Campaign campaign : activeCampaignsForImei) {
								if (!campaign.getUuid().equals(campaignUuid)) {
									multiCamp += campaign.getCampaignName();
									String firstStepStatus = deviceDetailRepository
											.findStatusByCampaignUuidAndStepOrderNumber(campaign.getUuid(), deviceId,
													1l);
									
									logger.info(msgUuid + " firstStepStatus  "+firstStepStatus +" "+ deviceId);
									if (!StringUtils.isEmpty(firstStepStatus)) {
										startedInAnyOtherCamp = true;
									}

								}
							}
							if (!startedInAnyOtherCamp) {
								deviceData.setDevice_status_for_campaign(DeviceStatusForCampaign.ELIGIBLE.getValue());
								deviceData.setComments(Constants.CONFLICT_ELIGIBLE + " " + multiCamp);
							}

							if (!StringUtils.isEmpty(campaignUuid)) {
								List<String> failedStepUuid = deviceDetailRepository.getFailedExecutedStep(campaignUuid,
										deviceId);
								if (failedStepUuid != null && failedStepUuid.size() > 0) {
									String stepStatus = deviceDetailRepository.getStatusOfStep(failedStepUuid.get(0),
											deviceId);
									logger.info(msgUuid + " stepStatus  "+stepStatus +" "+ deviceId);
									if (stepStatus.equalsIgnoreCase(CampaignStepDeviceStatus.PENDING.getValue())) {
										CampaignStep step = campaignStepRepository.findByUuid(failedStepUuid.get(0));
										deviceData.setComments(step.getFromPackage().getPackageName() + " to "
												+ step.getToPackage().getPackageName() + " "
												+ Constants.GATEWAY_PROBLEM_STATUS);
										deviceData.setDevice_status_for_campaign(
												DeviceStatusForCampaign.PROBLEM.getValue());
									}
								}
							}
						} else {
							deviceData.setDevice_status_for_campaign(DeviceStatusForCampaign.NOT_ELIGIBLE.getValue());
							deviceData.setComments(Constants.NOT_ELIGIBLE_FOR_BASELINE);

							for (Campaign campaign : activeCampaignsForImei) {
								if (!campaign.getUuid().equals(campaignUuid)) {
									String firstStepStatus = deviceDetailRepository
											.findStatusByCampaignUuidAndStepOrderNumber(campaign.getUuid(), deviceId,
													1l);
									logger.info(msgUuid + " firstStepStatus 1  "+firstStepStatus +" "+ deviceId);
									Long maxStep = campaignStepRepository.findLastStepByCampaignUuid(campaign.getUuid())
											.getStepOrderNumber();
									logger.info(msgUuid + " maxStep 1  "+maxStep +" "+ deviceId);
									String lastStepStatus = deviceDetailRepository
											.findStatusByCampaignUuidAndStepOrderNumber(campaign.getUuid(), deviceId,
													maxStep);
									logger.info(msgUuid + " lastStepStatus 1  "+lastStepStatus +" "+ deviceId);
									if (!StringUtils.isEmpty(firstStepStatus)
											&& (lastStepStatus == null || !lastStepStatus
													.equalsIgnoreCase(CampaignStepDeviceStatus.SUCCESS.getValue()))) {
										deviceData.setComments(
												Constants.CONFLICT_NOT_ELIGIBLE + " " + campaign.getCampaignName());
										break;
									}
								}
							}

						}
					}
				}

			}
		} catch (Exception e) {
			logger.error("error in calculating eligiblity for gateway {}", deviceId);
			e.printStackTrace();
		}

	}

	public static final List<Long> times = Arrays.asList(TimeUnit.DAYS.toMillis(365), TimeUnit.DAYS.toMillis(30),
			TimeUnit.DAYS.toMillis(1), TimeUnit.HOURS.toMillis(1), TimeUnit.MINUTES.toMillis(1),
			TimeUnit.SECONDS.toMillis(1));
	public static final List<String> timesString = Arrays.asList("year", "month", "day", "hour", "minute", "second");

	public static String toDuration(long duration) {
		/*
		 * this method is called in findALLCampaignListDisplay twice therefore, adding
		 * more checks here would be most effective
		 */

		StringBuffer res = new StringBuffer();
		for (int i = 0; i < times.size(); i++) {
			Long current = times.get(i);
			long temp = duration / current;
			if (temp > 0) {
				res.append(temp).append(" ").append(timesString.get(i)).append(temp != 1 ? "s" : "").append(" ago");
				break;
			}
		}
		if ("".equals(res.toString()))
			return "0 seconds ago";
		else
			return res.toString();
	}

	/*
	 * public static void main(String[] args) { String g =
	 * toDuration(Duration.between(Instant.now().minusSeconds(50400),
	 * Instant.now()).toMillis());
	 * 
	 * System.out.println(g); }
	 */

//	public void findALLCampaignListDisplay() {
//		logger.info("Inside findALLCampaignList  Custom Service Impl Method.");
//
//		List<CampaignListDisplay> campaignListDisplayList = new ArrayList<CampaignListDisplay>();
//		
//		List<Campaign>  campaignList = campaignRepo.findAllNonDeleted();
//		logger.info("total campaign list display size  "+campaignList.size());
//		for(Campaign campaign : campaignList)
//		{
//			logger.info("Inside findALLCampaignList Method Iteration For UUID : "+campaign.getCampaignName());
//			CampaignListDisplay campaignListDisplay = new CampaignListDisplay();
//			try{
//				campaignListDisplay.setDaysRunning(toDuration(Duration.between(campaign.getCampaignStartDate(),
//						Instant.now()).toMillis()));
//
//			}catch (Exception e){
//				logger.error("Problem Occured In Calculating Days Running.");
//				e.printStackTrace();
//			}
//
//			try {
//
//
//				if(deviceDetailRepository.findByUuidMax(campaign.getUuid())!=null){
//					Instant startExecutionTime = deviceDetailRepository.findByUuidMax(campaign.getUuid()).get(0).getStartExecutionTime();
//					campaignListDisplay.setLastCommandDays(toDuration(Duration.between(startExecutionTime,
//							Instant.now()).toMillis()));
//				}
//			}catch (Exception e){
//				logger.error("Problem Occured In Calculating Duration Days.");
//				e.printStackTrace();
//			}
//
//			campaignListDisplay.setCampaignListItemId(campaign.getCampaignId());
//			campaignListDisplay.setCampaignName(campaign.getCampaignName());
//			campaignListDisplay.setDescription(campaign.getDescription());
//			campaignListDisplay.setCampaignStatus(campaign.getCampaignStatus().getValue());
//			campaignListDisplay.setCreatedAt(campaign.getCreatedAt());
//			CampaignStatsPayloadList byUuid = getByUuid(campaign.getUuid(),new CampaignStatsPayloadList());
//			campaignListDisplay.setCompleted(byUuid.getCompleted().longValue());
//			campaignListDisplay.setEligible(byUuid.getEligible());
//			campaignListDisplay.setProblemCount(byUuid.getProblemCount());
//			campaignListDisplay.setCreatedBy(campaign.getCreatedBy().getFirstName()+" "+campaign.getCreatedBy().getLastName());
//			campaignListDisplay.setListUpdatedAt(Instant.now().toString());
//			campaignListDisplayList.add(campaignListDisplay);
//		}
//		logger.info("saving all for campaignListDisplay "+campaignListDisplayList.size());
//		campListRepo.saveAll(campaignListDisplayList);
//	
//	
//}	

	public void findALLCampaignListDisplay(String msgUuid) {
		analysisLog.info("Inside findALLCampaignList  Custom Service Impl Method.");

		List<CampaignListDisplay> campaignListDisplayList = new ArrayList<CampaignListDisplay>();

		List<Campaign> campaignList = campaignRepo.findAllNonDeleted();
		analysisLog.info("total campaign list display size  " + campaignList.size());
		for (Campaign campaign : campaignList) {
//			logger.info("Inside findALLCampaignList Method Iteration For UUID : "+campaign.getCampaignName());
			CampaignListDisplay campaignListDisplay = new CampaignListDisplay();
			try {
				campaignListDisplay.setDaysRunning(
						toDuration(Duration.between(campaign.getCampaignStartDate(), OffsetDateTime.now( ZoneId.of( "UTC" ) ).toInstant()).toMillis()));

			} catch (Exception e) {
				analysisLog.error("Problem Occured In Calculating Days Running.");
				e.printStackTrace();
			}

			try {
				if (!deviceDetailRepository.findByUuidMax(campaign.getUuid()).isEmpty()) {
					Instant startExecutionTime = deviceDetailRepository.findByUuidMax(campaign.getUuid()).get(0)
							.getStartExecutionTime();
					campaignListDisplay.setLastCommandDays(
							toDuration(Duration.between(startExecutionTime, OffsetDateTime.now( ZoneId.of( "UTC" ) ).toInstant()).toMillis()));
				}
			} catch (Exception e) {
				analysisLog.error("Problem Occured In Calculating Duration Days.");
				e.printStackTrace();
			}
			campaignListDisplay.setUuid(campaign.getUuid());
			campaignListDisplay.setCampaignListItemId(campaign.getCampaignId());
			campaignListDisplay.setCampaignName(campaign.getCampaignName());
			campaignListDisplay.setDescription(campaign.getDescription());

//			campaignUtils.checkForPausedCampaign(campaign);
//			campaignUtils.checkForCompletedCampaign(campaign);
			campaignListDisplay.setCampaignStatus(campaign.getCampaignStatus().getValue());
			campaignListDisplay.setCreatedAt(campaign.getCreatedAt());
			campaignListDisplay.setDeviceType(campaign.getDeviceType());
			CampaignStatsPayloadList byUuid = getByUuid(campaign.getUuid(), new CampaignStatsPayloadList(), msgUuid);
			campaignListDisplay.setCompleted(byUuid.getCompleted().longValue());
			campaignListDisplay.setEligible(byUuid.getEligible());
			campaignListDisplay.setProblemCount(byUuid.getProblemCount());
			campaignListDisplay.setInProgress(byUuid.getInProgress());
			campaignListDisplay.setNotStarted(byUuid.getNotStarted());
			campaignListDisplay.setNotInstalled(byUuid.getNotStarted());
			campaignListDisplay.setOffPath(byUuid.getOffPath());
			campaignListDisplay.setOnPath(byUuid.getOnPath());
			campaignListDisplay.setCampaignStartDate(campaign.getCampaignStartDate());
			if (campaign.getCreatedBy() != null) {
				campaignListDisplay.setCreatedBy(
						campaign.getCreatedBy().getFirstName() + " " + campaign.getCreatedBy().getLastName());
			}
			campaignListDisplay.setListUpdatedAt(OffsetDateTime.now( ZoneId.of( "UTC" ) ).toInstant().toString());
			campaignListDisplayList.add(campaignListDisplay);
		}
		analysisLog.info("saving all for campaignListDisplay " + campaignListDisplayList.size());
		campListRepo.saveAll(campaignListDisplayList);

	}

	@Override
	public CampaignInstalledDevice getCampaignInstalledDeviceFromMS(@Valid String imei) {

		CampaignInstalledDevice msDevices = new CampaignInstalledDevice();
				//iCampaignInstalledDeviceMsRepository
				//.findCampaignInstalledDeviceByDeviceImei(imei);

		// CampaignInstalledDevice msDevices =
		// restUtils.CampaignInstalledDeviceFromMS(imei);
		// System.out.println("msDevices "+msDevices.toString());
		return msDevices;
	}

	@Override
	public List<DeviceCampaignHistory> getDeviceCampaignHistoryByImei(String imei) {
		List<DeviceCampaignHistory> link = new ArrayList<DeviceCampaignHistory>();
		link = beanConverter.getDeviceCampaignHistoryByDeviceId(imei);
		return link;
	}

	@Override
	public CampaignHistory fetchCampaignHistory(String msgUuid, String deviceId) throws DeviceVersionException {
		logger.info(" deviceId:: ", deviceId + "msgUuid:: " + msgUuid);
		CampaignHistory campaignHistory = new CampaignHistory();
		ExecuteCampaignRequest executeCampaignRequest = new ExecuteCampaignRequest();
		List<String> imeis = new ArrayList<>();
		imeis.add(deviceId);
		List<DeviceReport> msDeviceReports = campaignDeviceHelper.getDeviceReports(imeis);
		if (msDeviceReports.size() == 0) {
			logger.info(" Device report not  found from redis deviceId:: ", deviceId + "msgUuid:: " + msgUuid);
			return campaignHistory;
		}
		campaignHistory.setPackagePayload(new PackagePayload());
		campaignHistory.setDeviceReport(msDeviceReports.get(0));

		executeCampaignRequest = beanConverter.deviceReportRedisToExecuteCampaignRequest(msDeviceReports.get(0));

		Campaign eligibleCampaign = null;
		List<Campaign> activeCampaignForImei;
		List<Campaign> excludeCampaignList = new ArrayList<Campaign>();
		List<String> campaignList = new ArrayList<String>();

		activeCampaignForImei = campaignUtils.checkCampaignForDeviceId(deviceId);

		for (Campaign campaign : activeCampaignForImei) {
			CampaignStep a = campaignStepRepository.findLastStepByCampaignUuid(campaign.getUuid());
			Long maxStepInCamp = a.getStepOrderNumber();
			String lastStepStatusInThisCamp = stepDeviceDetailRepository
					.findStatusByCampaignUuidAndStepOrderNumber(campaign.getUuid(), deviceId, maxStepInCamp);
			logger.info("Message UUID : 92 " + msgUuid + " lastStepStatusInThisCamp  " + lastStepStatusInThisCamp);
			analysisLog.debug("Message UUID  92 " + msgUuid + " lastStepStatusInThisCamp " + lastStepStatusInThisCamp);
			if (lastStepStatusInThisCamp != null
					&& lastStepStatusInThisCamp.equalsIgnoreCase(CampaignStepDeviceStatus.SUCCESS.getValue())) {
				excludeCampaignList.add(campaign);
			}
		}
		activeCampaignForImei.removeAll(excludeCampaignList);
		if (activeCampaignForImei == null || activeCampaignForImei.size() == 0) {
			logger.info("Message UUID : " + msgUuid + " Device  " + deviceId + " doesn't belong to any campaign");
			analysisLog.debug("Message UUID : " + msgUuid + " Device  " + deviceId + " doesn't belong to any campaign");
			return campaignHistory;
		} else {

			if (activeCampaignForImei.size() == 1) {
				eligibleCampaign = activeCampaignForImei.get(0);
				logger.info("Message UUID : 108 " + msgUuid);
				analysisLog.debug("Message UUID  108 " + msgUuid);
			} else {
				boolean campaignMatchFound = false;
				logger.info("Message UUID : 112 " + msgUuid);
				analysisLog.debug("Message UUID  112 " + msgUuid);
				for (Campaign activeCampaign : activeCampaignForImei) {
					eligibleCampaign = activeCampaign;
					campaignList.add(eligibleCampaign.getUuid());
					String campInProgressForGateway = stepDeviceDetailRepository
							.findStatusByCampaignUuidAndStepOrderNumber(activeCampaign.getUuid(), deviceId, 1l);
					logger.info(
							"Message UUID : 121 " + msgUuid + " campInProgressForGateway " + campInProgressForGateway);
					analysisLog.debug(
							"Message UUID  121 " + msgUuid + " campInProgressForGateway " + campInProgressForGateway);
					if (!StringUtils.isEmpty(campInProgressForGateway)) {
						campaignMatchFound = true;
						List<CampaignStep> allSteps = campaignStepRepository
								.getAllStepsOfCampaign(activeCampaign.getUuid());
						String campFinishedForGateway = stepDeviceDetailRepository
								.findStatusByCampaignUuidAndStepOrderNumber(activeCampaign.getUuid(), deviceId,
										allSteps.get(allSteps.size() - 1).getStepOrderNumber());
						logger.info(
								"Message UUID : 131 " + msgUuid + " campFinishedForGateway " + campFinishedForGateway);
						analysisLog.debug(
								"Message UUID  132 " + msgUuid + " campFinishedForGateway " + campFinishedForGateway);
						if (campFinishedForGateway != null) {
							if (!CampaignStepDeviceStatus.SUCCESS.getValue().equalsIgnoreCase(campFinishedForGateway)) {
								updateToPackageConfigInCampaign(executeCampaignRequest, allSteps, msgUuid);
								campFinishedForGateway = stepDeviceDetailRepository
										.findStatusByCampaignUuidAndStepOrderNumber(activeCampaign.getUuid(), deviceId,
												allSteps.get(allSteps.size() - 1).getStepOrderNumber());
								logger.info("Message UUID : 140 " + msgUuid + " campFinishedForGateway "
										+ campFinishedForGateway);
								analysisLog.debug("Message UUID  140 " + msgUuid + " campFinishedForGateway "
										+ campFinishedForGateway);
								if (!CampaignStepDeviceStatus.SUCCESS.getValue()
										.equalsIgnoreCase(campFinishedForGateway)) {
									break;
								} else {
									campaignMatchFound = false;
								}

							} else {
								campaignMatchFound = false;
							}
						} else {
							List<CampaignStep> allCampaignSteps = campaignStepRepository
									.findByCampaignUuid(activeCampaign.getUuid());
							CampaignStep campaignStep = allCampaignSteps.get(0);
							Package packageBaseLine = packageRepo.findByUuid(campaignStep.getFromPackage().getUuid());
							PackagePayload ackagePayload = beanConverter.packageToPackageResponse(packageBaseLine);
							campaignHistory.setPackagePayload(ackagePayload);

							return campaignHistory;

						}
					}
				}
				logger.info("Message UUID : 157 " + msgUuid + " campaignMatchFound " + campaignMatchFound);
				analysisLog.debug("Message UUID  157 " + msgUuid + " campaignMatchFound " + campaignMatchFound);
				if (!campaignMatchFound) {
					logger.info("Message UUID : " + msgUuid + " Looking for eligible campaign via baseline ");
					logger.info("Message UUID : " + msgUuid + " Num of camp " + activeCampaignForImei.size()
							+ " , campaign list: " + campaignList + " for device " + deviceId);
					analysisLog.debug("Message UUID : " + msgUuid + " Looking for eligible campaign via baseline ");
					analysisLog.debug("Message UUID : " + msgUuid + " Num of camp " + activeCampaignForImei.size()
							+ " , campaign list: " + campaignList + " for device " + deviceId);

					for (Campaign activeCampaign : activeCampaignForImei) {
						logger.info("Message UUID : " + msgUuid + " camp " + activeCampaign.getCampaignName());
						analysisLog.debug("Message UUID : " + msgUuid + " camp " + activeCampaign.getCampaignName());

						Map<String, List<DeviceReport>> deviceMaintReportMap = new HashMap<String, List<DeviceReport>>();
						DeviceReport dr = new DeviceReport();
						dr.setAPP_SW_VERSION(executeCampaignRequest.getSwVersionApplication());
						dr.setBASEBAND_SW_VERSION(executeCampaignRequest.getSwVersionBaseband());
						dr.setDEVICE_ID(deviceId);
						dr.setEXTENDER_VERSION(executeCampaignRequest.getExtenderVersion());
						dr.setConfig1CIV(executeCampaignRequest.getConfig1CIV());
						dr.setConfig2CIV(executeCampaignRequest.getConfig2CIV());
						dr.setConfig3CIV(executeCampaignRequest.getConfig3CIV());
						dr.setConfig4CIV(executeCampaignRequest.getConfig4CIV());
						dr.setConfig1CRC(executeCampaignRequest.getConfig1CRC());
						dr.setConfig2CRC(executeCampaignRequest.getConfig2CRC());
						dr.setConfig3CRC(executeCampaignRequest.getConfig3CRC());
						dr.setConfig4CRC(executeCampaignRequest.getConfig4CRC());
						dr.setConfigurationDesc(executeCampaignRequest.getConfigurationDesc());
						List<DeviceReport> drList = new ArrayList<DeviceReport>();
						drList.add(dr);
						deviceMaintReportMap.put(deviceId, drList);

						List<CampaignStep> allSteps = campaignStepRepository
								.getAllStepsOfCampaign(activeCampaign.getUuid());
						String basePackageUuid = allSteps.get(0).getFromPackage().getUuid();
						Package baseLine = packageRepository.findByUuid(basePackageUuid);

						System.out.println(baseLine.getAppVersion() + " " + baseLine.getBleVersion());

						logger.info("Message UUID : " + msgUuid + " baseline " + baseLine.getPackageName());
						String isEligible = campaignUtils.checkDeviceMatchesBaseline(deviceId, deviceMaintReportMap,
								baseLine, msgUuid);
						if (isEligible != null
								&& isEligible.equalsIgnoreCase(DeviceStatusForCampaign.ELIGIBLE.getValue())) {
							eligibleCampaign = activeCampaign;
							List<CampaignStep> allCampaignSteps = campaignStepRepository
									.findByCampaignUuid(activeCampaign.getUuid());
							CampaignStep campaignStep = allCampaignSteps.get(0);
							Package packageBaseLine = packageRepo.findByUuid(campaignStep.getFromPackage().getUuid());
							PackagePayload ackagePayload = beanConverter.packageToPackageResponse(packageBaseLine);
							campaignHistory.setPackagePayload(ackagePayload);
							return campaignHistory;
						}
					}
				}

			}
		}

		return campaignHistory;
	}

	private void updateToPackageConfigInCampaign(ExecuteCampaignRequest executeCampaignRequest,
			List<CampaignStep> allSteps, String msgUuid) {

		CampaignStepDeviceDetail currentStepInfo = null;
		CampaignStep campStep = null;
		logger.info("Message UUID : 333 " + msgUuid + " updateToPackageConfigInCampaign ");
		analysisLog.debug("Message UUID  333 " + msgUuid + " updateToPackageConfigInCampaign ");

		if (allSteps != null && allSteps.size() > 0) {
			logger.info("Message UUID : 337 " + msgUuid + " allSteps " + allSteps);
			analysisLog.debug("Message UUID  337 " + msgUuid + " allSteps " + allSteps);
			for (CampaignStep campaignStep : allSteps) {
				Package toPackage = campaignStep.getToPackage();

				logger.info("Message UUID : " + msgUuid
						+ ", Comparing toPackage fields with executeCampaignRequest fields in updateToPackageConfigInCampaign method; "
						+ "toPackage all fields: " + toPackage.toString() + " , executeCampaignRequest all fields:  "
						+ executeCampaignRequest.toString());
				if ((toPackage.getAppVersion().equalsIgnoreCase(Constants.ANY)
						|| toPackage.getAppVersion().equalsIgnoreCase(executeCampaignRequest.getSwVersionApplication()))
						&& (toPackage.getBinVersion().equalsIgnoreCase(Constants.ANY) || toPackage.getBinVersion()
								.equalsIgnoreCase(executeCampaignRequest.getSwVersionBaseband()))
						&& (toPackage.getBleVersion().equalsIgnoreCase(Constants.ANY)
								|| toPackage.getBleVersion().equalsIgnoreCase(executeCampaignRequest.getBleVersion()))
						&& (toPackage.getConfig1Crc().equalsIgnoreCase(Constants.ANY)
								|| toPackage.getConfig1Crc().equalsIgnoreCase(executeCampaignRequest.getConfig1CRC())
								|| (executeCampaignRequest.getConfig1CRC() == null && toPackage.getConfig1()
										.equalsIgnoreCase(executeCampaignRequest.getConfigurationDesc())))
						&& (toPackage.getConfig2Crc().equalsIgnoreCase(Constants.ANY)
								|| toPackage.getConfig2Crc().equalsIgnoreCase(executeCampaignRequest.getConfig2CRC())
								|| executeCampaignRequest.getConfig2CRC() == null)
						&& (toPackage.getConfig3Crc().equalsIgnoreCase(Constants.ANY)
								|| toPackage.getConfig3Crc().equalsIgnoreCase(executeCampaignRequest.getConfig3CRC())
								|| executeCampaignRequest.getConfig3CRC() == null)
						&& (toPackage.getConfig4Crc().equalsIgnoreCase(Constants.ANY)
								|| toPackage.getConfig4Crc().equalsIgnoreCase(executeCampaignRequest.getConfig4CRC())
								|| executeCampaignRequest.getConfig4CRC() == null)
						&& (toPackage.getMcuVersion().equalsIgnoreCase(Constants.ANY) || toPackage.getMcuVersion()
								.equalsIgnoreCase(executeCampaignRequest.getExtenderVersion()))) {
					logger.info("Message UUID : " + msgUuid + ", Conditions fulfilled for Campaign: "
							+ campaignStep.getCampaign().getUuid());
					campStep = campaignStep;
					break;
				}

			}
		} else
			logger.info("Message UUID : 361 " + msgUuid + " campStep " + campStep);
		analysisLog.debug("Message UUID  361 " + msgUuid + " campStep " + campStep);
		if (campStep != null) {

			currentStepInfo = stepDeviceDetailRepository
					.findStatusByCampaignUuidAndStepUuid(executeCampaignRequest.getDeviceId(), campStep.getUuid());
			logger.info("Message UUID : 367 " + msgUuid + " currentStepInfo " + currentStepInfo);
			analysisLog.debug("Message UUID  367 " + msgUuid + " currentStepInfo " + currentStepInfo);
			if (currentStepInfo != null && currentStepInfo.getStatus() != null
					&& (currentStepInfo.getStatus().equals(CampaignStepDeviceStatus.PENDING))) {
				currentStepInfo.setStatus(CampaignStepDeviceStatus.SUCCESS);
				currentStepInfo.setStopExecutionTime(OffsetDateTime.now( ZoneId.of( "UTC" ) ).toInstant());
				stepDeviceDetailRepository.save(currentStepInfo);
				logger.info("Message UUID : " + msgUuid + " Device ID " + executeCampaignRequest.getDeviceId()
						+ " successfully upgraded to package  : " + campStep.getToPackage().getPackageName());
				analysisLog.debug("Message UUID : " + msgUuid + " Device ID " + executeCampaignRequest.getDeviceId()
						+ " successfully upgraded to package  : " + campStep.getToPackage().getPackageName());
			}

		}
	}

	@Override
	public PackageSequence getPackageSequenceByCampaignName(String campaignName, String msgUuid) {
		logger.info("Message UUID : " + msgUuid);

		String allImei = null;

		long totalGateways = 0;
		long onHold = 0;
		long notStarted = 0;
		long inProgress = 0;
		long completed = 0;

		PackageSequence packageSequence = new PackageSequence();
		List<PackagePayload> packagePaloadList = new ArrayList<>();
		Campaign campaignData = campaignRepo.findByCampaignName(campaignName);
		List<CampaignStep> allCampaignSteps = campaignStepRepository.findByCampaignUuid(campaignData.getUuid());

		if (allCampaignSteps != null) {
			allCampaignSteps.forEach(campaignStep -> {
				packagePaloadList.add(beanConverter.packageToPackageResponse(campaignStep.getFromPackage()));
			});
			packagePaloadList.add(beanConverter
					.packageToPackageResponse(allCampaignSteps.get(packagePaloadList.size() - 1).getToPackage()));
			packageSequence.setPackagePayloadList(packagePaloadList);
		}

		Long maxStepInCamp = campaignStepRepository.findLastStepByCampaignUuid(campaignData.getUuid())
				.getStepOrderNumber();

		List<MsDeviceRestResponse> allDevices = new ArrayList<MsDeviceRestResponse>();
		if (campaignData.getGroup().getGroupingType().equals("Customer")) {
			//allDevices = restUtils.getDevicesFromMSByCustomerName(campaignData.getGroup().getGroupingName());
			allDevices =  deviceVersionRepository.findImeisByCustomerName(campaignData.getGroup().getGroupingName());

		} else {
			allImei = campaignData.getGroup().getTargetValue();
			if (!StringUtils.isEmpty(allImei)) {
				//allDevices = restUtils.getSelectedDevicesFromMSByImeis(Arrays.asList(allImei.split(",")));
				allDevices = deviceVersionRepository.findAllDeviceByIMEIList(Arrays.asList(allImei.split(",")));

			}
		}

		long notEligibleCount = 0;
		List<String> allImeis = allDevices.stream().map(MsDeviceRestResponse::getImei).collect(Collectors.toList());

		totalGateways = allDevices == null ? 0 : allDevices.size();
		completed = deviceDetailRepository.getCountOfLastCampaignStepStatusAsSuccess(campaignData.getUuid(),
				maxStepInCamp, CampaignStepDeviceStatus.SUCCESS);

		onHold = multiCampaignDeviceRepository.findByCampaignUuid(campaignData.getUuid()).size();

		inProgress = deviceDetailRepository.getInProgressButNotHoldGatewayCount(campaignData.getUuid());// ,

		if (campaignData.getGroup().getGroupingType().equals("Customer")) {
			//allDevices = restUtils.getDevicesFromMSByCustomerName(campaignData.getGroup().getGroupingName());
			allDevices =  deviceVersionRepository.findImeisByCustomerName(campaignData.getGroup().getGroupingName());

		} else {
			allImei = campaignData.getGroup().getTargetValue();
			if (!StringUtils.isEmpty(allImei)) {
				//allDevices = restUtils.getSelectedDevicesFromMSByImeis(Arrays.asList(allImei.split(",")));
				allDevices = deviceVersionRepository.findAllDeviceByIMEIList(Arrays.asList(allImei.split(",")));

			}
		}

		List<DeviceReport> msDeviceReports = campaignDeviceHelper.getDeviceReports(allImeis);
		String basePackageUuid = allCampaignSteps.get(0).getFromPackage().getUuid();
		Package baseLine = packageRepository.findByUuid(basePackageUuid);

		List<StepStatusForDeviceView> firstStepStatusInThisCampForAllDevices = new ArrayList<StepStatusForDeviceView>();
		List<StepStatusForDeviceView> lastStepStatusInThisCampForAllDevices = new ArrayList<StepStatusForDeviceView>();

		if (allImeis != null && !allImeis.isEmpty()) {
			firstStepStatusInThisCampForAllDevices = deviceDetailRepository
					.findAllStatusByCampaignUuidAndStepOrderNumber(campaignData.getUuid(), allImeis, 1l);
			lastStepStatusInThisCampForAllDevices = deviceDetailRepository
					.findAllStatusByCampaignUuidAndStepOrderNumber(campaignData.getUuid(), allImeis, maxStepInCamp);

		}
		Map<String, List<StepStatusForDeviceView>> firstStepStatusInThisCampMap = firstStepStatusInThisCampForAllDevices
				.stream().collect(Collectors.groupingBy(w -> w.getDevice_id()));
		Map<String, List<StepStatusForDeviceView>> lastStepStatusInThisCampMap = lastStepStatusInThisCampForAllDevices
				.stream().collect(Collectors.groupingBy(w -> w.getDevice_id()));

		Map<String, DeviceReport> msDeviceReportsMap = new HashMap<>();

		if (msDeviceReports != null && msDeviceReports.size() > 0) {
			for (DeviceReport dev : msDeviceReports) {
				msDeviceReportsMap.put(dev.getDEVICE_ID(), dev);
			}
		}
		long baseLineMatch = 0;
		long offPath = 0;
		long onPath = 0;

		for (MsDeviceRestResponse device : allDevices) {
			String firstStepStatusInThisCamp = null;
			// String lastStepStatusInThisCamp = null;
			List<StepStatusForDeviceView> firstStepStatusInThisCampList = firstStepStatusInThisCampMap
					.get(device.getImei());
			if (firstStepStatusInThisCampList != null && !firstStepStatusInThisCampList.isEmpty()) {
				firstStepStatusInThisCamp = firstStepStatusInThisCampList.get(0).getStatus();
			}

			List<StepStatusForDeviceView> lastStepStatusInThisCampList = lastStepStatusInThisCampMap
					.get(device.getImei());

			Map<String, List<DeviceReport>> deviceMaintReportMap = msDeviceReports.stream()
					.collect(Collectors.groupingBy(w -> w.getDEVICE_ID()));

			String isEligible = campaignUtils.checkDeviceMatchesBaseline(device.getImei(), deviceMaintReportMap,
					baseLine, msgUuid);

			if (!isEligible.equalsIgnoreCase(DeviceStatusForCampaign.ELIGIBLE.getValue())
					&& (firstStepStatusInThisCamp == null)) {

				//// NEEDED changes
				notEligibleCount++;
				List<Campaign> campaignList = campaignUtils.checkCampaignForDeviceId(device.getImei(), null);

				if (campaignList != null && campaignList.size() <= 1) {
					offPath++;
				}
			}
			baseLineMatch += campaignUtils
					.validateDeviceUpgradeEligibleForCampaign(msDeviceReportsMap.get(device.getImei())) == true ? 1 : 0;
		}

		totalGateways = allDevices == null ? 0 : allDevices.size();
		completed = deviceDetailRepository.getCountOfLastCampaignStepStatusAsSuccess(campaignData.getUuid(),
				maxStepInCamp, CampaignStepDeviceStatus.SUCCESS);

		onHold = multiCampaignDeviceRepository.findByCampaignUuid(campaignData.getUuid()).size();
		inProgress = deviceDetailRepository.getInProgressButNotHoldGatewayCount(campaignData.getUuid());// ,

		notStarted = totalGateways - (onHold + inProgress + completed + notEligibleCount);

		packageSequence.setCampaignSummary(new CampaignSummary(totalGateways, notStarted, inProgress, completed, onHold,
				notEligibleCount, baseLineMatch, offPath, onPath));
		return packageSequence;
	}

	@Override
	public String getCampaignUUidByName(String name) {
		Campaign campaign = campaignRepo.findByCampaignName(name);
		return campaign.getUuid();
	}

	@Override
	public List<CampaignListDisplay> findALLCampaignDataList(Long userId) {

		User user = restUtils.getUserFromAuthService(userId);
		logger.info("Inside findALLCampaignDataList service level");
		List<String> role = user.getRole().stream().map(Role::getName).collect(Collectors.toList());
		List<CampaignListDisplay> list = new ArrayList<>();
		if (role.contains(AuthoritiesConstants.SUPER_ADMIN)) {
			list = campListRepo.findAll();
		}
		logger.info("Fetched CampaignListDisplay " + list.size());
		return list;
	}

	@Override
	public CurrentCampaignResponse currentCampaignByImei(String imei, String msgUuid)
			throws DeviceInMultipleCampaignsException {
		logger.info("imei:   " + imei + " msgUuid :" + msgUuid);
		CurrentCampaignResponse currentCampaignResponse = null;
		Campaign eligibleCampaign = null;
		List<Campaign> activeCampaignForImei;
		List<Campaign> excludeCampaignList = new ArrayList<Campaign>();
		List<String> campaignList = new ArrayList<String>();

		List<VersionMigrationDetailDTO> versionMigrationDetailList = new ArrayList<>();

		// list of active campaigns for particular device
		activeCampaignForImei = campaignUtils.checkCampaignForDeviceId(imei, null);
		for (Campaign campaign : activeCampaignForImei) {

			// find last step-details of campaign (repo call)
			CampaignStep a = campaignStepRepository.findLastStepByCampaignUuid(campaign.getUuid());
			Long maxStepInCamp = a.getStepOrderNumber();

			// find status of last step : completed/pending (should not be failed)
			String lastStepStatusInThisCamp = stepDeviceDetailRepository
					.findStatusByCampaignUuidAndStepOrderNumber(campaign.getUuid(), imei, maxStepInCamp);

			if (lastStepStatusInThisCamp != null
					&& lastStepStatusInThisCamp.equalsIgnoreCase(CampaignStepDeviceStatus.SUCCESS.getValue())) {
				excludeCampaignList.add(campaign);
			}
		}

		activeCampaignForImei.removeAll(excludeCampaignList);

		if (activeCampaignForImei == null || activeCampaignForImei.size() == 0) {
			throw new DeviceVersionException("Device  " + imei + " doesn't belong to any campaign");
		} else {

			if (activeCampaignForImei.size() == 1) {
				eligibleCampaign = activeCampaignForImei.get(0);
				// take other variable for eligible camp
			} else {
				boolean campaignMatchFound = false;
				for (Campaign activeCampaign : activeCampaignForImei) {
					eligibleCampaign = activeCampaign;
					campaignList.add(eligibleCampaign.getUuid());

					// find status of first step for given device id for selected active campaign
					String campInProgressForGateway = stepDeviceDetailRepository
							.findStatusByCampaignUuidAndStepOrderNumber(activeCampaign.getUuid(), imei, 1l);

					if (!StringUtils.isEmpty(campInProgressForGateway)) {
						campaignMatchFound = true;

						// fetch all steps data for selected campaign
						List<CampaignStep> allSteps = campaignStepRepository
								.getAllStepsOfCampaign(activeCampaign.getUuid());

						// fetch status of second last step where status in not failed
						String campFinishedForGateway = stepDeviceDetailRepository
								.findStatusByCampaignUuidAndStepOrderNumber(activeCampaign.getUuid(), imei,
										allSteps.get(allSteps.size() - 1).getStepOrderNumber());
						if (campFinishedForGateway != null) {
							if (!CampaignStepDeviceStatus.SUCCESS.getValue().equalsIgnoreCase(campFinishedForGateway)) {

								// fetch status of second last step where status in not failed
								campFinishedForGateway = stepDeviceDetailRepository
										.findStatusByCampaignUuidAndStepOrderNumber(activeCampaign.getUuid(), imei,
												allSteps.get(allSteps.size() - 1).getStepOrderNumber());
								if (!CampaignStepDeviceStatus.SUCCESS.getValue()
										.equalsIgnoreCase(campFinishedForGateway)) {
									break;
								} else {
									campaignMatchFound = false;
								}

							} else {
								campaignMatchFound = false;
							}
						} else {
							break;
						}
					}
				}

			}

		}

		currentCampaignResponse = new CurrentCampaignResponse();

		if (eligibleCampaign != null) {
			List<CampaignStep> allCampaignSteps = campaignStepRepository.findByCampaignUuid(eligibleCampaign.getUuid());
			currentCampaignResponse.setCampaignName(eligibleCampaign.getCampaignName());
			currentCampaignResponse.setCampaignUuid(eligibleCampaign.getUuid());

			if (allCampaignSteps != null) {
				for (CampaignStep campaignStep : allCampaignSteps) {
					VersionMigrationDetailDTO versionMigrationDetailDTO = null;
					versionMigrationDetailDTO = new VersionMigrationDetailDTO();
					versionMigrationDetailDTO
							.setFromPackage(beanConverter.packageToPackageResponse(campaignStep.getFromPackage()));
					versionMigrationDetailDTO
							.setToPackage(beanConverter.packageToPackageResponse(campaignStep.getToPackage()));
					versionMigrationDetailDTO.setStepOrderNumber(campaignStep.getStepOrderNumber());
					versionMigrationDetailDTO.setAtCommand(campaignStep.getAtCommand());
					versionMigrationDetailDTO.setStepId(campaignStep.getStepId());
					versionMigrationDetailList.add(versionMigrationDetailDTO);
				}
			}
			currentCampaignResponse.setVersionMigrationDetailDtoList(versionMigrationDetailList);
		}
		return currentCampaignResponse;
	}

	@Override
	public void resetStatusOfProblemDevice(List<String> selectedDeviceIdForResetStatus) {
		if (selectedDeviceIdForResetStatus != null && selectedDeviceIdForResetStatus.size() > 0) {
			campaignRepo.deleteFailedStepFromCampaignStepDeviceDetail(selectedDeviceIdForResetStatus);
		}

	}

	@Override
	public List<CampaignIdAndNameView> getIdenticalBaselinePackageFromExistingCampaign(String packageUuid) {
		List<CampaignIdAndNameView> existingCampaigns = null;
		Package package1 = packageRepository.findByUuid(packageUuid);
		List<Package> identical = isIdenticalPackageExistInBaseline(package1);
		if (identical != null && identical.size() > 0) {
			List<String> uuids = identical.stream().map(x -> x.getUuid()).collect(Collectors.toList());
			existingCampaigns = campaignStepRepository.getBaselinePackageFromExistingCampaign(uuids);
		}
		return existingCampaigns;
	}

	private List<Package> isIdenticalPackageExistInBaseline(Package package1) {
		return packageRepository.findIdentical(package1.getAppVersion().trim().toLowerCase(),
				package1.getBinVersion().trim().toLowerCase(), package1.getBleVersion().trim().toLowerCase(),
				package1.getMcuVersion().trim().toLowerCase(), package1.getConfig1().trim().toLowerCase(),
				package1.getConfig2().trim().toLowerCase(), package1.getConfig3().trim().toLowerCase(),
				package1.getConfig4().trim().toLowerCase(), package1.getConfig1Crc().trim().toLowerCase(),
				package1.getConfig2Crc().trim().toLowerCase(), package1.getConfig3Crc().trim().toLowerCase(),
				package1.getConfig4Crc().trim().toLowerCase(), package1.getDeviceType().trim().toLowerCase(),
				package1.getLiteSentryHardware().trim().toLowerCase(), package1.getLiteSentryApp().trim().toLowerCase(),
				package1.getLiteSentryBoot().trim().toLowerCase(), package1.getMicrospMcu().trim().toLowerCase(),
				package1.getMicrospApp().trim().toLowerCase(), package1.getCargoMaxbotixHardware().trim().toLowerCase(),
				package1.getCargoMaxbotixFirmware().trim().toLowerCase(),
				package1.getCargoRiotHardware().trim().toLowerCase(),
				package1.getCargoRiotFirmware().trim().toLowerCase(), package1.getPackageId());
	}

	@Override
	public Page<DeviceWithEligibility> getAllDeviceFromMSPage(String customerName, int page, Integer pageSize,
			String sort, String order, String basePackageUuid, String campaignUuid, String msgUuid) {
		Page<MsDeviceRestResponse> msDevices = restUtils.getDeviceDataFromMS(customerName, page, pageSize, sort, order);

		List<DeviceWithEligibility> deviceDataList = new ArrayList<DeviceWithEligibility>();
		DeviceWithEligibility deviceData = null;
		Package baseLine = packageRepo.findByUuid(basePackageUuid);

		List<String> allImeis = msDevices.stream().map(MsDeviceRestResponse::getImei).collect(Collectors.toList());
		List<DeviceReport> msDeviceReports = campaignDeviceHelper.getDeviceReports(allImeis);
		Map<String, List<DeviceReport>> deviceMaintReportMap = msDeviceReports.stream()
				.collect(Collectors.groupingBy(w -> w.getDEVICE_ID()));

		Long maxStepInCamp = null;

		if (!StringUtils.isEmpty(campaignUuid)) {
			maxStepInCamp = campaignStepRepository.findLastStepByCampaignUuid(campaignUuid).getStepOrderNumber();
		}

		for (MsDeviceRestResponse device : msDevices) {
			deviceData = new DeviceWithEligibility();
			gatewayEligibilityCalculation(device.getImei(), baseLine, deviceData, campaignUuid, maxStepInCamp,
					deviceMaintReportMap, msgUuid);
			String deviceId = device.getImei();
			deviceData.setDEVICE_ID(deviceId);
			deviceData.setOWNER_LEVEL_2(device.getOrganisationName());
			deviceData.setDEVICE_MODEL(device.getProductName());
			deviceData.setOrganisationName(device.getOrganisationName());
			deviceData.setImei(deviceId);
			deviceDataList.add(deviceData);

		}
		Page<DeviceWithEligibility> page1 = new PageImpl<>(deviceDataList, msDevices.getPageable(),
				msDevices.getTotalElements());

		return page1;

	}

	@Override
	public List<StepDTO> getStepUuidDTO(String campaignUuid) {
		List<Tuple> findByStepDtoByUuid = deviceDetailRepository.findByStepDtoByUuid(campaignUuid);
		List<StepDTO> stockTotalDto = null;
		stockTotalDto = findByStepDtoByUuid.stream().map(t -> new StepDTO(t.get(0, String.class),
				t.get(1, BigInteger.class), t.get(2, String.class), t.get(3, String.class)))
				.collect(Collectors.toList());
		return stockTotalDto;
	}

	@Override
	public void processDevicesForUpdateCampaign(List<String> campaignUuids, String msgUuid) {
		logger.info(msgUuid + " . Inside processDevicesForUpdateCampaign  .");

		CampaignStatsPayload campaignResponse = null;

		for (String campaignUuid : campaignUuids) {
			logger.info(msgUuid + " . Inside processDevicesForUpdateCampaign serv meth loop.");
			Campaign campaignData = campaignRepo.findByUuid(campaignUuid);
 			if (campaignData == null) {
				throw new DeviceVersionException("Campaign not found for id..............");
			}
			List<CampaignStep> allCampaignSteps = campaignStepRepository.findByCampaignUuid(campaignData.getUuid());
			logger.info(msgUuid + " . calling beanConverter.campaignToCampaignStatsResponse from processDevicesForUpdateCampaign ");

			deviceDetailProcessor.processDevicesForUpdateCampaign(campaignData, allCampaignSteps, msgUuid);
			campaignResponse = beanConverter.campaignToCampaignStatsResponseNew(campaignData, allCampaignSteps,
					msgUuid);

			logger.info(msgUuid + " . returning response");
		}

	}
  	@Override
  	public void processDevicesForUpdateCampaignSchedular(String msgUuid,String status) {
		analysisLog.info("Inside processDevicesForUpdateCampaignSchedular Method. msgUuid  "+msgUuid);
		logger.info("MSG UUID:  "+msgUuid  );

  		List<Campaign> campaignList = campaignRepo.findAllNonDeleted();
 		for (Campaign campaign : campaignList) {
			try {
				if(status.equals("IN_PROGRESS") && campaign.getCampaignStatus().equals("IN_PROGRESS")) {
					List<CampaignStep> allCampaignSteps = campaignStepRepository.findByCampaignUuid(campaign.getUuid());
					logger.info(msgUuid + " Status: " +campaign.getCampaignStatus() + " " +campaign.getCampaignName() + " "+ campaign.getUuid());
	 				deviceDetailProcessor.processDevicesForUpdateCampaign(campaign, allCampaignSteps, msgUuid);
					beanConverter.campaignToCampaignStatsResponseNew(campaign, allCampaignSteps, msgUuid);
					logger.info("MSG UUID:  "+msgUuid +" Campaign Completed ###  CAMPAIGN NAME : "+campaign.getCampaignName() + " CAMPAIGN UUID "+ campaign.getUuid() +" STATUS: "+ campaign.getCampaignStatus());

 				}else if (status.equals("PAUSED") && campaign.getCampaignStatus().equals("PAUSED")) {
 					List<CampaignStep> allCampaignSteps = campaignStepRepository.findByCampaignUuid(campaign.getUuid());
					logger.info(msgUuid
							+ " calling beanConverter.campaignToCampaignStatsResponse from processDevicesForUpdateCampaign ");
	 				deviceDetailProcessor.processDevicesForUpdateCampaign(campaign, allCampaignSteps, msgUuid);
					beanConverter.campaignToCampaignStatsResponseNew(campaign, allCampaignSteps, msgUuid);
					logger.info("MSG UUID:  "+msgUuid +" Campaign Completed ###  CAMPAIGN NAME : "+campaign.getCampaignName() + " CAMPAIGN UUID "+ campaign.getUuid() +" STATUS: "+ campaign.getCampaignStatus());

 				}
 				else if (status.equals("NOT_STARTED") && campaign.getCampaignStatus().equals("NOT_STARTED")) {
 					List<CampaignStep> allCampaignSteps = campaignStepRepository.findByCampaignUuid(campaign.getUuid());
					logger.info(msgUuid
							+ " calling beanConverter.campaignToCampaignStatsResponse from processDevicesForUpdateCampaign ");
	 				deviceDetailProcessor.processDevicesForUpdateCampaign(campaign, allCampaignSteps, msgUuid);
					beanConverter.campaignToCampaignStatsResponseNew(campaign, allCampaignSteps, msgUuid);
					logger.info("MSG UUID:  "+msgUuid +" Campaign Completed ###  CAMPAIGN NAME : "+campaign.getCampaignName() + " CAMPAIGN UUID "+ campaign.getUuid() +" STATUS: "+ campaign.getCampaignStatus());
 				}
 				else if (status.equals("STOPPED") && campaign.getCampaignStatus().equals("STOPPED") ) {
 					List<CampaignStep> allCampaignSteps = campaignStepRepository.findByCampaignUuid(campaign.getUuid());
					logger.info(msgUuid
							+ " calling beanConverter.campaignToCampaignStatsResponse from processDevicesForUpdateCampaign ");
	 				deviceDetailProcessor.processDevicesForUpdateCampaign(campaign, allCampaignSteps, msgUuid);
					beanConverter.campaignToCampaignStatsResponseNew(campaign, allCampaignSteps, msgUuid);
					logger.info("MSG UUID:  "+msgUuid +" Campaign Completed ###  CAMPAIGN NAME : "+campaign.getCampaignName() + " CAMPAIGN UUID "+ campaign.getUuid() +" STATUS: "+ campaign.getCampaignStatus());

 				}
 				else if (status.equals("RETIRED") && campaign.getCampaignStatus().equals("RETIRED")) {
 					List<CampaignStep> allCampaignSteps = campaignStepRepository.findByCampaignUuid(campaign.getUuid());
					logger.info(msgUuid
							+ " calling beanConverter.campaignToCampaignStatsResponse from processDevicesForUpdateCampaign ");
	 				deviceDetailProcessor.processDevicesForUpdateCampaign(campaign, allCampaignSteps, msgUuid);
					beanConverter.campaignToCampaignStatsResponseNew(campaign, allCampaignSteps, msgUuid);
					logger.info("MSG UUID:  "+msgUuid +" Campaign Completed ###  CAMPAIGN NAME : "+campaign.getCampaignName() + " CAMPAIGN UUID "+ campaign.getUuid() +" STATUS: "+ campaign.getCampaignStatus());

 				}
 			} catch (Exception e) {
 				logger.info(msgUuid + " Got exception while scheduler  processDevicesForUpdateCampaign  "+msgUuid);
				logger.info("MSG UUID:  "+msgUuid +" Campaign Failed ###  CAMPAIGN NAME : "+campaign.getCampaignName() + " CAMPAIGN UUID "+ campaign.getUuid() +" STATUS: "+ campaign.getCampaignStatus());


			}

 
		}

	}
 
	
}
