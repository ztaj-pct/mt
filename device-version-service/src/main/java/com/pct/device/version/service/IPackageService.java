package com.pct.device.version.service;

import com.pct.device.version.model.Package;
import com.pct.device.version.payload.PackagePayload;
import com.pct.device.version.payload.SavePackageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;



/**
 * @author dhruv
 *
 */
public interface IPackageService {


	void savePackage(List<SavePackageRequest> savePackageRequest , String userName);
	
	Page<PackagePayload> getAllPackage(Map<String, String> filterValues , Pageable pageable, boolean sortByInUse, String order,String userName);
	
	Boolean getPackageByName(String packageName);

	void deleteById(String id);

	PackagePayload getByUuid(String packageUuid);

	void update(PackagePayload packageRequest, String userName);

	List<Package> fetchALLPackagesForCsv(HttpServletResponse response) throws IOException;

	public void fetchFilterdPackagesForCsv(List<PackagePayload> payloadList, HttpServletResponse response) throws IOException;

	Boolean getEmergencyStopFlagValue();

	Boolean updateEmergencyStopFlagValue(Boolean isEmergencyStop);


//    Boolean updateGatewayMacAddress(UpdateMacAddressRequest updateMacAddressRequest, Long userId);

}
