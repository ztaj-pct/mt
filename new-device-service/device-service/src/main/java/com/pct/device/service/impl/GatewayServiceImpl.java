package com.pct.device.service.impl;

import com.pct.common.constant.GatewayStatus;
import com.pct.common.constant.GatewayType;
import com.pct.common.constant.SensorStatus;
import com.pct.common.model.*;
import com.pct.device.bean.GatewayBean;
import com.pct.device.exception.DeviceException;
import com.pct.device.payload.*;
import com.pct.device.repository.IAssetGatewayXrefRepository;
import com.pct.device.repository.IGatewayRepository;
import com.pct.device.repository.IGatewaySensorXrefRepository;
import com.pct.device.repository.ISensorRepository;
import com.pct.device.repository.projections.GatewayIdAndImeiView;
import com.pct.device.service.IGatewayService;
import com.pct.device.specification.GateWaySpecification;
import com.pct.device.util.BeanConverter;
import com.pct.device.util.Constants;
import com.pct.device.util.RestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class GatewayServiceImpl implements IGatewayService {

    @Autowired
    private IGatewayRepository gatewayRepository;
    @Autowired
    private ISensorRepository sensorRepository;
    @Autowired
    private IGatewaySensorXrefRepository gatewaySensorXrefRepository;
    @Autowired
    private IAssetGatewayXrefRepository assetGatewayXrefRepository;
    @Autowired
    private BeanConverter beanConverter;
    @Autowired
    private RestUtils restUtils;

    @Override
    public FetchGatewayImeiResponse getGatewayImeiResponse(Long imei) {
        List<GatewayIdAndImeiView> gatewayIdAndImeiView = gatewayRepository.findIdAndImei(imei);
        FetchGatewayImeiResponse gatewayImeiResponse = new FetchGatewayImeiResponse();
        gatewayImeiResponse.setGatewayImeiList(gatewayIdAndImeiView);
        return gatewayImeiResponse;
    }

    @Override
    public List<GatewayBean> getGateway(String accountNumber, GatewayStatus status) {
        List<GatewayBean> gatewayBeanList = new ArrayList<>();
        List<Gateway> gatewayList = new ArrayList<Gateway>();
        if (status == null) {
            gatewayList = gatewayRepository.findByAccountNumber(accountNumber);
        } else {
            gatewayList = gatewayRepository.findByAccountNumberAndStatus(accountNumber, status);
        }
//        if (gatewayList.size() > 0)
//        	gatewayBeanList = gatewayList.stream().map(beanConvertor::convertGatewayToGatewayBean).collect(Collectors.toList());
        return gatewayBeanList;
    }
    

    @Override
    public List<GatewayBean> getGateway(String accountNumber, String imei, String gatewayUuid, GatewayStatus gatewayStatus, GatewayType type, String macAddress) {
        List<GatewayBean> gatewayBeanList = new ArrayList<>();
        List<Gateway> gatewayList = new ArrayList<Gateway>();
        Specification<Gateway> spc = GateWaySpecification.getGatewayListSpecification(accountNumber, imei, gatewayUuid, gatewayStatus, type, macAddress);
        gatewayList = gatewayRepository.findAll(spc);

        if (gatewayList.size() > 0) {
            gatewayList.forEach(gateway -> {
                List<GatewaySensorXref> gatewaySensorXrefs = gatewaySensorXrefRepository.findByGatewayUuid(gateway.getUuid());
                gatewayBeanList.add(beanConverter.convertGatewayToGatewayBean(gateway, gatewaySensorXrefs));
            });
        }
        return gatewayBeanList;
    }

    @Override
    @Transactional
    public Map<String, List<String>> saveShipmentDetails(ShipmentDetailsRequest shipmentDetailsRequest) {
        Company company = restUtils.getCompanyFromCompanyService(shipmentDetailsRequest.getSalesforceAccountId());
        Map<String, List<String>> responseMap = new HashMap<>();
        if (company != null) {
            List<Gateway> gatewayList = new ArrayList<>();
            for (AssetDetail assetDetail : shipmentDetailsRequest.getAssetDetails()) {
                for (String imei : assetDetail.getImeiList()) {
                	if(imei != null) {
                    Gateway byImei = gatewayRepository.findByImei(imei);
                    if (byImei == null) {
                        Gateway gateway = new Gateway();
                        gateway.setStatus(GatewayStatus.PENDING);
                        gateway.setCompany(company);
                        gateway.setType(GatewayType.GATEWAY);
                        gateway.setCreatedOn(Instant.now());
                        gateway.setImei(imei);
                        gateway.setProductCode(assetDetail.getProductCode());
                        gateway.setProductName(assetDetail.getProductShortName());
                        boolean isGatewayUuidUnique = false;
                        String gatewayUuid = "";
                        while (!isGatewayUuidUnique) {
                            gatewayUuid = UUID.randomUUID().toString();
                            Gateway byUuid = gatewayRepository.findByUuid(gatewayUuid);
                            if (byUuid == null) {
                                isGatewayUuidUnique = true;
                            }
                        }
                        gateway.setUuid(gatewayUuid);
                        gateway.setSalesforceOrderId(shipmentDetailsRequest.getSalesforceOrderNumber());
                        gateway = gatewayRepository.save(gateway);
                        if (assetDetail.getSensorList() != null && !assetDetail.getSensorList().isEmpty()) {
                            for (SensorProduct sensorProduct : assetDetail.getSensorList()) {
                                Sensor sensor = new Sensor();
                                sensor.setStatus(SensorStatus.PENDING);
                                sensor.setGateway(gateway);
                                sensor.setProductCode(sensorProduct.getProductCode());
                                sensor.setProductName(sensorProduct.getProductName());
                                sensor.setCreatedOn(Instant.now());
                                boolean isSensorUuidUnique = false;
                                String sensorUuid = "";
                                while (!isSensorUuidUnique) {
                                    sensorUuid = UUID.randomUUID().toString();
                                    Gateway byUuid = gatewayRepository.findByUuid(sensorUuid);
                                    if (byUuid == null) {
                                        isSensorUuidUnique = true;
                                    }
                                }
                                sensor.setUuid(sensorUuid);
                                sensor = sensorRepository.save(sensor);
                                GatewaySensorXref gatewaySensorXref = new GatewaySensorXref();
                                gatewaySensorXref.setDateCreated(Instant.now());
                                gatewaySensorXref.setIsActive(true);
                                gatewaySensorXref.setGateway(gateway);
                                gatewaySensorXref.setSensor(sensor);
                                gatewaySensorXref = gatewaySensorXrefRepository.save(gatewaySensorXref);
                            }
                        }
                        if (responseMap.get(Constants.SAVED_MAP_KEY) != null) {
                            responseMap.get(Constants.SAVED_MAP_KEY).add(imei);
                        } else {
                            List<String> imeiList = new ArrayList<>();
                            imeiList.add(imei);
                            responseMap.put(Constants.SAVED_MAP_KEY, imeiList);
                        }
                    } else {
                        if (responseMap.get(Constants.REJECTED_MAP_KEY) != null) {
                            responseMap.get(Constants.REJECTED_MAP_KEY).add(imei);
                        } else {
                            List<String> imeiList = new ArrayList<>();
                            imeiList.add(imei);
                            responseMap.put(Constants.REJECTED_MAP_KEY, imeiList);
                        }
                    }
                }else {
                    throw new DeviceException("IMEI number can not be null");
                }
                }
            }
        } else {
            throw new DeviceException("No Company found for account number " + shipmentDetailsRequest.getSalesforceAccountName());
        }
        return responseMap;
    }
    
    @Override
    @Transactional
	public Map<String, List<String>> saveBeaconDetails(BeaconDetailsRequest beaconDetailsRequest,Long userId) {
    	Company company = restUtils.getCompanyFromCompanyService(beaconDetailsRequest.getSalesforceAccountId());
        Map<String, List<String>> responseMap = new HashMap<>();
        if (company != null) {
            for (BeaconPayload beaconPayload : beaconDetailsRequest.getBeaconDetails()) {
            	if(beaconPayload.getMacAddress() != null) {
            		Gateway gate = gatewayRepository.findByMac_address(beaconPayload.getMacAddress());
            		 User user = restUtils.getUserFromAuthService(userId);
            		if(gate==null) {
                        Gateway gateway = new Gateway();
                        gateway.setStatus(GatewayStatus.PENDING);
                        gateway.setCompany(company);
                        gateway.setType(GatewayType.BEACON);
                        gateway.setImei("");
                        gateway.setCreatedOn(Instant.now());
                        gateway.setCreatedBy(user);
                        gateway.setProductCode(beaconPayload.getProductCode());
                        gateway.setProductName(beaconPayload.getProductShortName());
                        gateway.setMacAddress(beaconPayload.getMacAddress());
                        boolean isGatewayUuidUnique = false;
                        String gatewayUuid = "";
                        while (!isGatewayUuidUnique) {
                            gatewayUuid = UUID.randomUUID().toString();
                            Gateway byUuid = gatewayRepository.findByUuid(gatewayUuid);
                            if (byUuid == null) {
                                isGatewayUuidUnique = true;
                            }
                        }
                        gateway.setUuid(gatewayUuid);
                        gateway.setSalesforceOrderId(beaconDetailsRequest.getSalesforceOrderNumber());
                        gateway = gatewayRepository.save(gateway);
            		}else {
            			throw new DeviceException("Mac Address is already associated with another device");
            		}
                       }else {
                        throw new DeviceException("Mac Address can not be null");
                    }
            }
            
        } else {
            throw new DeviceException("No Company found for account number " + beaconDetailsRequest.getSalesforceAccountName());
        }
        return responseMap;
            
        
	}

    @Override
    public Boolean updateGatewayMacAddress(UpdateMacAddressRequest updateMacAddressRequest, Long userId) {
        User user = restUtils.getUserFromAuthService(userId);
        Gateway gateway = gatewayRepository.findByUuid(updateMacAddressRequest.getUuid());
        gateway.setMacAddress(updateMacAddressRequest.getMacAddress());
        gateway.setUpdatedOn(Instant.ofEpochMilli(Long.parseLong(updateMacAddressRequest.getDatetimeRT())));
        gateway.setUpdatedBy(user);
        gateway = gatewayRepository.save(gateway);
        return true;
    }

    @Override
    public Boolean resetGateway(String can, String imei) {
        if (can != null && !can.isEmpty()) {
            Company company = restUtils.getCompanyFromCompanyService(can);
            if (imei != null && !imei.isEmpty()) {
                Gateway gateway = gatewayRepository.findByImeiAndAccountNumber(imei, can);
                Boolean installDeleteStatus = restUtils.deleteInstallDataForGateway(gateway.getUuid());
                boolean status = deleteGatewayData(gateway);
                return installDeleteStatus && status;
            } else {
                List<Gateway> gatewayList = gatewayRepository.findByAccountNumber(can);
                Boolean installDeleteStatus = restUtils.deleteInstallDataForCompany(company.getUuid());
                AtomicBoolean status = new AtomicBoolean(true);
                gatewayList.forEach(gateway -> {
                    status.set(status.get() && deleteGatewayData(gateway));
                });
                return status.get();
            }
        } else {
            throw new DeviceException("Please provide customer account number");
        }
    }
    
    @Override
    public Boolean resetGatewayWithMac(String can, String mac) {
        if (can != null && !can.isEmpty()) {
            Company company = restUtils.getCompanyFromCompanyService(can);
            if (mac != null && !mac.isEmpty()) {
                Gateway gateway = gatewayRepository.findByMACAndAccountNumber(mac, can);
                Boolean installDeleteStatus = restUtils.deleteInstallDataForGateway(gateway.getUuid());
                boolean status = deleteGatewayData(gateway);
                return installDeleteStatus && status;
            } else {
                List<Gateway> gatewayList = gatewayRepository.findByAccountNumber(can);
                Boolean installDeleteStatus = restUtils.deleteInstallDataForCompany(company.getUuid());
                AtomicBoolean status = new AtomicBoolean(true);
                gatewayList.forEach(gateway -> {
                    status.set(status.get() && deleteGatewayData(gateway));
                });
                return status.get();
            }
        } else {
            throw new DeviceException("Please provide customer account number");
        }
    }

    private Boolean deleteGatewayData(Gateway gateway) {
        List<GatewaySensorXref> gatewaySensorXrefList = gatewaySensorXrefRepository.findByGatewayUuid(gateway.getUuid());
        gatewaySensorXrefList.forEach(gatewaySensorXref -> {
            gatewaySensorXrefRepository.delete(gatewaySensorXref);
        });
        gateway.getSensors().forEach(sensor -> {
            sensorRepository.delete(sensor);
        });
        List<AssetGatewayXref> assetGatewayXrefList = assetGatewayXrefRepository.findByGatewayId(gateway.getId());
        assetGatewayXrefList.forEach(assetGatewayXref -> {
            assetGatewayXrefRepository.delete(assetGatewayXref);
        });
        gatewayRepository.delete(gateway);
        return true;
    }

	@Override
	public String getGatewayInstallationStatus(String imei) {
		return gatewayRepository.findStatusByImei(imei);
	}

	

	
}
