package com.pct.device.service;

import java.util.List;

import com.pct.device.payload.SavePackageRequest;

public interface IPackageService {


	void savePackage(List<SavePackageRequest> savePackageRequest , Long userId);

//    Boolean updateGatewayMacAddress(UpdateMacAddressRequest updateMacAddressRequest, Long userId);

}
