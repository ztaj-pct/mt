package com.pct.device.service.impl;


import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import com.pct.common.constant.DeviceStatus;
import com.pct.common.model.Asset;
import com.pct.common.model.Asset_Device_xref;
import com.pct.common.model.Device;
import com.pct.common.model.Organisation;
import com.pct.common.model.User;
import com.pct.device.exception.DeviceException;
import com.pct.device.exception.ManufacturerNotFoundException;
import com.pct.device.payload.AssetDeviceAssociationPayLoad;
import com.pct.device.payload.AssetDeviceAssociationPayLoadForIA;
import com.pct.device.payload.InstallationResponse;
import com.pct.device.repository.IAssetDeviceXrefRepository;
import com.pct.device.repository.IAssetRepository;
import com.pct.device.repository.IDeviceRepository;

import com.pct.device.service.IAssetDeviceService;
import com.pct.device.util.BeanConverter;
import com.pct.device.util.RestUtils;

@Service
public class AssetDeviceServiceImpl implements IAssetDeviceService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AssetDeviceServiceImpl.class);

	@Autowired
	IAssetRepository assetRepo;
	
	@Autowired
	IAssetDeviceXrefRepository assetDeviceRepo;

	@Autowired
	IDeviceRepository deviceRepo;

	@Autowired
	private RestUtils restUtils;
	
	@Autowired
	private BeanConverter beanConverter;
	
	@Autowired
	private IAssetRepository assetRepository;


	@Override
	public Asset_Device_xref addAssetDeviceAssociation(
			AssetDeviceAssociationPayLoad assetDeviceAssociationPayLoad, String userName) throws Exception {
		LOGGER.info("Inside addAssetDeviceAssociation service for device " + assetDeviceAssociationPayLoad.getImei());
		Asset asset = new Asset();
		if(assetDeviceAssociationPayLoad.getVin()!=null) {
		 asset = assetRepo.findByVin(assetDeviceAssociationPayLoad.getVin());
		}else {
		 asset = assetRepo.findByAssignedName(assetDeviceAssociationPayLoad.getAssetId());
		}
		Device device = deviceRepo.findByImei(assetDeviceAssociationPayLoad.getImei());
		User user = restUtils.getUserFromAuthService(userName);
		Asset_Device_xref af;
		if (asset != null && device != null) {
			System.out.println("vin is empty !!");
			af = new Asset_Device_xref();
			af.setAsset(asset);
			af.setDevice(device);
			af.setDateCreated(Instant.now());
			af.setActive(true);
			af.setCreatedBy(user);
			af.setUpdatedBy(user);
			assetDeviceRepo.save(af);
		LOGGER.info("Asset device association saved successfully for device Id" + assetDeviceAssociationPayLoad.getImei());
		}
		// user create
		else {
			throw new Exception("Device status is not in pending state for Uuid = "
					+ assetDeviceAssociationPayLoad.getVin() + "" + assetDeviceAssociationPayLoad.getImei());
		}
		return af;
	}

	@Override
	public Asset_Device_xref getAssetDeviceAssociation(String deviceId, String username) {
		Asset_Device_xref assetDeviceDetail = assetDeviceRepo.findByDevice(deviceId);
		return assetDeviceDetail;
	}
	
	public Asset_Device_xref AssetDeviceDisassociation(String deviceId, String username) {
		Asset_Device_xref assetDeviceDisassociate = assetDeviceRepo.deleteByDevice(deviceId);
		return assetDeviceDisassociate;
	}

	public List<Asset_Device_xref> getAssetDeviceHistory(String deviceId, String username) {
		List<Asset_Device_xref> assetDeviceHistory = assetDeviceRepo.findAllByDevice(deviceId);
		return assetDeviceHistory;
	}

	@Override
	public Asset_Device_xref createAssetDeviceAssociationForIA(
			AssetDeviceAssociationPayLoadForIA assetDeviceAssociationPayLoadForIA, String username) throws Exception {
		LOGGER.info("Inside createAssetDeviceAssociationForIA service for device ID: "
				+ assetDeviceAssociationPayLoadForIA.getImei() + " VIN no: " + assetDeviceAssociationPayLoadForIA.getVin()
				+ " Asset ID: " + assetDeviceAssociationPayLoadForIA.getAssetId());
		Asset asset = null;
		Asset_Device_xref asset_device_xref = null;
		if (assetDeviceAssociationPayLoadForIA.getVin() != null
				&& !"".equalsIgnoreCase(assetDeviceAssociationPayLoadForIA.getVin())) {
			asset = assetRepo.findByVin(assetDeviceAssociationPayLoadForIA.getVin());
		} else {
			asset = assetRepo.findByAssignedName(assetDeviceAssociationPayLoadForIA.getAssetId());
		}
		Device device = deviceRepo.findByImei(assetDeviceAssociationPayLoadForIA.getImei());
		User user = restUtils.getUserFromAuthService(username);
		
		if (asset != null && device != null) {
			device.setStatus(DeviceStatus.ACTIVE);
			device = deviceRepo.saveAndFlush(device);
			asset_device_xref = createAssetDeviceAssociation(asset, device, user);
			LOGGER.info("Asset device association saved successfully for device Id"
					+ assetDeviceAssociationPayLoadForIA.getImei() + " sset device association id : " + asset_device_xref.getId());
		} else if(asset == null && device != null) {
			Organisation com = null;
			if (assetDeviceAssociationPayLoadForIA.getAccountNumber() != null) {
				com = device.getOrganisation();
				LOGGER.info("after rest call from get company", com);
				if (com == null) {
					com = restUtils.getCompanyFromCompanyService(assetDeviceAssociationPayLoadForIA.getAccountNumber());
				}
			}
			asset = beanConverter.assetsPayloadToAssets(assetDeviceAssociationPayLoadForIA, com, true, user, null);
			if ((assetDeviceAssociationPayLoadForIA.getManufacturer() != null && !assetDeviceAssociationPayLoadForIA.getManufacturer().isEmpty())
					&& asset.getManufacturer() == null) {
				throw new ManufacturerNotFoundException("Invalid Manufacturer: Manufacturer name is not valid.");
			}
			
			asset = assetRepository.saveAndFlush(asset);
			device.setStatus(DeviceStatus.ACTIVE);
			device = deviceRepo.saveAndFlush(device);
			asset_device_xref = createAssetDeviceAssociation(asset, device, user);
			LOGGER.info("Asset device association saved successfully for device Id"
					+ assetDeviceAssociationPayLoadForIA.getImei() + " sset device association id : " + asset_device_xref.getId());
		
		} else {
			throw new Exception("Device status is not in pending state for Uuid = "
					+ assetDeviceAssociationPayLoadForIA.getVin() + "" + assetDeviceAssociationPayLoadForIA.getImei());
		}
		return asset_device_xref;
	}

	private Asset_Device_xref createAssetDeviceAssociation(Asset asset, Device device, User user) {

		Asset_Device_xref asset_device_xref = new Asset_Device_xref();
		asset_device_xref.setAsset(asset);
		asset_device_xref.setDevice(device);
		asset_device_xref.setDateCreated(Instant.now());
		asset_device_xref.setActive(true);
		asset_device_xref.setCreatedBy(user);
		asset_device_xref.setUpdatedBy(user);
		assetDeviceRepo.save(asset_device_xref);
		return asset_device_xref;

	}
	
	public List<InstallationResponse> getTraillerLookup(String assetId, String imeiId) {
		List<Asset_Device_xref> asset_Device_xrefs = null;
		if(assetId != null) {
			 asset_Device_xrefs = assetDeviceRepo.findByAssetId(assetId);
		} else if(imeiId != null) {
			 asset_Device_xrefs = assetDeviceRepo.findAllByDeviceId(imeiId);	
		} else {
			throw new DeviceException("Please provide assetId or imeiId");
		}
		return beanConverter.convertAssetDeviceXrefToInstalledResponse(asset_Device_xrefs);
	}


}
