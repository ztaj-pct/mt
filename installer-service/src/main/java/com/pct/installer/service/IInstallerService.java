package com.pct.installer.service;

import com.pct.installer.dto.CustomerInventoryDTO;

/**
 * @author Abhishek on 02/06/20
 */

public interface IInstallerService {

    CustomerInventoryDTO inventoryForCustomer(String logUUId, String accountNumber);


	CustomerInventoryDTO getInventoryForCustomerWithImprovement(String logUUId, String accountNumber);

}
