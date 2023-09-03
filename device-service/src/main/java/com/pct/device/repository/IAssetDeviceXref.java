package com.pct.device.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

//import com.pct.common.model.AssetGatewayXref;
import com.pct.common.model.Asset_Device_xref;
import com.pct.device.service.device.DeviceCommand;
@Repository
public interface IAssetDeviceXref extends JpaRepository<Asset_Device_xref, Long>, JpaSpecificationExecutor<DeviceCommand>{

}
