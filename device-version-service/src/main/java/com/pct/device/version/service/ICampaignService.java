package com.pct.device.version.service;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import com.pct.device.service.device.CampaignInstalledDevice;
import com.pct.device.version.dto.StepDTO;
import com.pct.device.version.exception.DeviceInMultipleCampaignsException;
import com.pct.device.version.exception.DeviceVersionException;
import com.pct.device.version.model.CampaignListDisplay;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pct.device.version.payload.CampaignHistory;
import com.pct.device.version.payload.CampaignPayload;
import com.pct.device.version.payload.CampaignStatsPayload;
import com.pct.device.version.payload.CurrentCampaignResponse;
import com.pct.device.version.payload.DeviceCampaignHistory;
import com.pct.device.version.payload.DeviceWithEligibility;
import com.pct.device.version.payload.PackageSequence;
import com.pct.device.version.payload.SaveCampaignRequest;
import com.pct.device.version.payload.SelectedDevice;
import com.pct.device.version.payload.UpdateCampaignPayload;
import com.pct.device.version.repository.projections.CampaignIdAndNameView;



/**
 * @author dhruv
 *
 */
public interface ICampaignService {


	String saveCampaign(SaveCampaignRequest saveCampaignRequest , Long userId);
	
	CampaignStatsPayload getByUuid(String packageUuid , String msgUuid,int method);

	Page<CampaignPayload> getAllCampaign(Map<String, String> filterValues , Pageable pageable);

	void deleteById(String uuid);

	String update(UpdateCampaignPayload campaignToUpdate, String userName);

	List<DeviceWithEligibility> getAllDeviceFromMS(String customerName, int i, Integer pageSize, String sort, String order, String basePackageUuid, String campaignUuid , String msgUuid);

	SelectedDevice getSelectedDevices(List<String> imeiList, int i, Integer pageSize, String sort, String order, String basePackageUuid, String campaignUuid , String msgUuid);

	CampaignInstalledDevice getCampaignInstalledDeviceFromMS(@Valid String imei);

	List<CampaignIdAndNameView> getBaselinePackageFromExistingCampaign(String packageUuid);
	
	List<DeviceCampaignHistory> getDeviceCampaignHistoryByImei(String imei);
	
	Boolean getCampaignByName(String campaignName);

	public Page<CampaignListDisplay> findALLCampaignList(Map<String, String> filterValues , Pageable pageable,String userName);

	public CampaignHistory fetchCampaignHistory(String msgUuid, String deviceId)
			throws DeviceVersionException;
	
	public PackageSequence getPackageSequenceByCampaignName(String campaignUuid, String msgUuid);
	
    String getCampaignUUidByName(String campaignName);
    
    public List<CampaignListDisplay> findALLCampaignDataList(Long userId);
    
    public CurrentCampaignResponse currentCampaignByImei(String imei, String msgUuid) throws DeviceInMultipleCampaignsException;

	void resetStatusOfProblemDevice(List<String> selectedDeviceIdForResetStatus);

	List<CampaignIdAndNameView> getIdenticalBaselinePackageFromExistingCampaign(String packageUuid);
	Page<DeviceWithEligibility> getAllDeviceFromMSPage(String customerName, int i, Integer pageSize, String sort, String order, String basePackageUuid, String campaignUuid , String msgUuid);

	List<StepDTO> getStepUuidDTO(String campaignUuid);

	public void processDevicesForUpdateCampaign(List<String> campaignUuids, String msgUuid);
	public void processDevicesForUpdateCampaignSchedular(String uuid,String status);
}
