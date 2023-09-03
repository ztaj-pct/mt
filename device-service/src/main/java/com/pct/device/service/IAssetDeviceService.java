package com.pct.device.service;

import com.pct.common.model.Asset_Device_xref;
import com.pct.device.payload.AssetDeviceAssociationPayLoad;
import com.pct.device.payload.AssetDeviceAssociationPayLoadForIA;

public interface IAssetDeviceService {

	Asset_Device_xref addAssetDeviceAssociation(AssetDeviceAssociationPayLoad assetDeviceAssociationPayLoad,
			String userName) throws Exception;

	Asset_Device_xref getAssetDeviceAssociation(String deviceId, String username);

	Asset_Device_xref createAssetDeviceAssociationForIA(AssetDeviceAssociationPayLoadForIA assetDeviceAssociationPayLoadForIA,
			String username) throws Exception;

}
