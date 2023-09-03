package com.pct.installer.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pct.common.constant.OrganisationRole;
import com.pct.common.dto.AssetDTO;
import com.pct.common.dto.OrganisationDtoForInventory;
//import com.pct.common.model.Company;
import com.pct.common.model.Device;
//import com.pct.common.model.Gateway;
import com.pct.common.model.Organisation;
import com.pct.installer.dto.CustomerInventoryDTO;
import com.pct.installer.dto.InventoryListAssetDTO;
import com.pct.installer.dto.InventoryListBeaconDTO;
import com.pct.installer.dto.InventoryListDTO;
import com.pct.installer.dto.InventoryListGatewayDTO;
import com.pct.installer.service.IInstallerService;
import com.pct.installer.util.BeanConverter;
import com.pct.installer.util.RestUtils;

/**
 * @author Abhishek on 02/06/20
 */

@Service
public class InstallerService implements IInstallerService {

    @Autowired
    private RestUtils restUtils;
    @Autowired
    private BeanConverter beanConverter;

    @Override
    public CustomerInventoryDTO inventoryForCustomer(String logUUId, String accountNumber) {
        CustomerInventoryDTO inventoryForCustomer = new CustomerInventoryDTO();
        List<InventoryListDTO> inventoryListDTOList = new ArrayList<>();
        if (accountNumber != null && !accountNumber.isEmpty()) {
            inventoryListDTOList.add(getInventoryForCustomer(logUUId, accountNumber));
        } else {
            inventoryListDTOList = getInventoryForAllCustomers(logUUId);
        }

        inventoryForCustomer.setInventoryList(inventoryListDTOList);
        return inventoryForCustomer;
    }

    private InventoryListDTO getInventoryForCustomer(String logUUId, String accountNumber) {
    	Organisation company = restUtils.getCompanyFromCompanyService(logUUId, accountNumber);
        List<InventoryListAssetDTO> inventoryListAssetDTOList = null;
        List<InventoryListGatewayDTO> inventoryListGatewayDTOList = null;
        List<InventoryListBeaconDTO> inventoryListBeaconListDTO = null;
        List<AssetDTO> assets = restUtils.getAssetsByCANAndStatusFromDeviceServiceUsingDTO(logUUId, accountNumber, "PENDING");
        List<Device> gateways = restUtils.getGatewaysByCANAndStatusFromDeviceService(logUUId, accountNumber, "PENDING");
        gateways.addAll(restUtils.getGatewaysByCANAndStatusFromDeviceService(logUUId, accountNumber, "INSTALL_IN_PROGRESS"));
        inventoryListAssetDTOList = beanConverter.createInventoryListAssetDTOFromAssetDTOs(assets);
        inventoryListGatewayDTOList = beanConverter.createInventoryListGatewayDTOFromGateways(gateways);
        InventoryListDTO inventoryListDTO = new InventoryListDTO();
        inventoryListDTO.setAssets(inventoryListAssetDTOList);
        inventoryListDTO.setGateways(inventoryListGatewayDTOList);
        /**added beacon values**/
        inventoryListBeaconListDTO = beanConverter.createInventoryListBeaconDTOFromGateways(gateways);
        /**added beacon values**/
        inventoryListDTO.setBeacon(inventoryListBeaconListDTO);
        inventoryListDTO.setAccountNumber(accountNumber);
        inventoryListDTO.setCustomerName(company.getOrganisationName());
        inventoryListDTO.setRequireAssetList(company.getIsAssetListRequired());        
        inventoryListDTO.setRequireAssetList(company.getIsAssetListRequired() != null ? company.getIsAssetListRequired() : Boolean.FALSE);
        inventoryListDTO.setCanMaintenance(company.getMaintenanceMode() != null ? company.getMaintenanceMode() : Boolean.FALSE);
        return inventoryListDTO;
    }

    private List<InventoryListDTO> getInventoryForAllCustomers(String logUUId) {
        List<InventoryListDTO> inventoryListDTOList = new ArrayList<>();
        List<Organisation> customers = restUtils.getCompanyByTypeFromCompanyService(logUUId, OrganisationRole.END_CUSTOMER.getValue());
        customers.forEach(customer -> {
            inventoryListDTOList.add(getInventoryForCustomer(logUUId, customer.getAccountNumber()));
        });
        return inventoryListDTOList;
    }
    
    
    @Override
    public CustomerInventoryDTO getInventoryForCustomerWithImprovement(String logUUId, String accountNumber) {
        CustomerInventoryDTO inventoryForCustomer = new CustomerInventoryDTO();
        List<InventoryListDTO> inventoryListDTOList = new ArrayList<>();
        if (accountNumber != null && !accountNumber.isEmpty()) {
            inventoryListDTOList.add(convertInventoryForCustomer(accountNumber));
        } else {
            inventoryListDTOList = getInventoryForAllCustomersWithImprovement(logUUId);
        }

        inventoryForCustomer.setInventoryList(inventoryListDTOList);
        return inventoryForCustomer;
    }
    
    private InventoryListDTO convertInventoryForCustomer(String accountNumber) {
        Organisation company = restUtils.getCompanyFromCompanyService("",accountNumber);
        InventoryListDTO inventoryListDTO = new InventoryListDTO();
        inventoryListDTO.setAccountNumber(accountNumber);
        inventoryListDTO.setCustomerName(company.getOrganisationName());
        inventoryListDTO.setRequireAssetList(company.getIsAssetListRequired());
        inventoryListDTO.setCanMaintenance(company.getMaintenanceMode());
        inventoryListDTO.setRequireAssetList(company.getIsAssetListRequired() != null ? company.getIsAssetListRequired() : Boolean.FALSE);
        inventoryListDTO.setCanMaintenance(company.getMaintenanceMode() != null ? company.getMaintenanceMode() : Boolean.FALSE);
        
        return inventoryListDTO;
    }
    
    private InventoryListDTO convertInventoryForCustomerObj(Organisation company) {
        InventoryListDTO inventoryListDTO = new InventoryListDTO();
        inventoryListDTO.setAccountNumber(company.getAccountNumber());
        inventoryListDTO.setCustomerName(company.getOrganisationName());
        inventoryListDTO.setRequireAssetList(company.getIsAssetListRequired());
        inventoryListDTO.setCanMaintenance(company.getMaintenanceMode());
        inventoryListDTO.setRequireAssetList(company.getIsAssetListRequired() != null ? company.getIsAssetListRequired() : Boolean.FALSE);
        inventoryListDTO.setCanMaintenance(company.getMaintenanceMode() != null ? company.getMaintenanceMode() : Boolean.FALSE);
        return inventoryListDTO;
    }

    private List<InventoryListDTO> getInventoryForAllCustomersWithImprovement(String logUUId) {
        List<InventoryListDTO> inventoryListDTOList = new ArrayList<>();
        List<OrganisationDtoForInventory> customers = restUtils.getCustomerCompanyDto(logUUId);
        customers.forEach(customer -> {
            inventoryListDTOList.add(convertInventoryForCustomerObjDto(customer));
        });
        return inventoryListDTOList;
    }
    
    private InventoryListDTO convertInventoryForCustomerObjDto(OrganisationDtoForInventory company) {
        InventoryListDTO inventoryListDTO = new InventoryListDTO();
        inventoryListDTO.setAccountNumber(company.getAccountNumber());
        inventoryListDTO.setCustomerName(company.getOrganisationName());
        inventoryListDTO.setRequireAssetList(company.getIsAssetListRequired());
        inventoryListDTO.setCanMaintenance(company.getMaintenanceMode());
        inventoryListDTO.setRequireAssetList(company.getIsAssetListRequired() != null ? company.getIsAssetListRequired() : Boolean.FALSE);
        inventoryListDTO.setCanMaintenance(company.getMaintenanceMode() != null ? company.getMaintenanceMode() : Boolean.FALSE);
        return inventoryListDTO;
    }
    
    
}
