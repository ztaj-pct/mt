package com.pct.organisation.service;

import com.pct.common.dto.OrganisationDtoForInventory;
import com.pct.common.model.Organisation;
import com.pct.organisation.dto.CustomerWithLocation;
import com.pct.organisation.payload.CustomerLocationPayload;

import java.util.List;
import java.util.Map;

/**
 * @author Abhishek on 12/05/20
 */
public interface ICustomerService {

	Organisation getCompanyByAccountNumber(String accountNumber);

	List<String> getCompanyByType(String type);

	List<Organisation> getListOfCompanyByType(String type,String name);

	Map<String, List<CustomerLocationPayload>> getCustomerAndLocationByUser(String userUuid);
	
	CustomerWithLocation getCustomerWithLocationByUser(String userUuid);

	Boolean resetCompanyData(String companyUuid);

	Organisation getCompanyByUuid(String uuid);

	public List<Organisation> getListOfAllOrganisationByType(String type);

	Organisation getCompanyById(Long id);

	List<Organisation> findAllCustomer();
	
	List<Organisation> getCustomerCompany();

	Map<String, List<CustomerLocationPayload>> getAccessListByUserName(String userName);

	List<OrganisationDtoForInventory> getCustomerCompanyDto();
}
