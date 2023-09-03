package com.pct.device.service;

import java.util.List;
import java.util.Map;

import com.pct.device.dto.AssetToDeviceDTO;
import com.pct.device.dto.RyderApiDTO;
import com.pct.device.payload.AddAssetResponse;
import com.pct.device.payload.AssestAssociationRequest;
import com.pct.device.payload.AssestDissociationRequest;
import com.pct.device.payload.AssestReassignmentRequest;

public interface IReportBuilderService {

	AddAssetResponse assestAssociation(AssestAssociationRequest assestAssociationRequest, String can, String msgUuid);

	AddAssetResponse assetReassignment(AssestReassignmentRequest assetReassignmentRequest, String can, String msgUuid);

	AddAssetResponse assetDissociation(AssestDissociationRequest assetDissociationRequest, String can, String msgUuid);

	AssetToDeviceDTO getAssetAssociationDetails(String assetId, String can, String msgUuid);

	List<RyderApiDTO> ryderApi(String assetName, String imei, String imei_last5, String vin, String msgUuid);

	Map<String, Object> mailReport();

	
}
