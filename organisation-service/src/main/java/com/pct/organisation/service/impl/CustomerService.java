package com.pct.organisation.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.pct.common.constant.CompanyType;
import com.pct.common.constant.OrganisationRole;
import com.pct.common.dto.OrganisationDtoForInventory;
import com.pct.common.model.Organisation;
import com.pct.common.model.User;
import com.pct.common.util.JwtUser;
import com.pct.organisation.exception.BadRequestException;
import com.pct.organisation.exception.OrganisationException;
import com.pct.organisation.dto.CustomerWithLocation;
import com.pct.organisation.payload.CustomerLocationPayload;
import com.pct.organisation.repository.IOrganisationRepository;
import com.pct.organisation.service.ICustomerService;
import com.pct.organisation.util.AuthoritiesConstants;
import com.pct.organisation.util.BeanConvertor;
import com.pct.organisation.util.RestUtils;

@Service
public class CustomerService implements ICustomerService {
	Logger logger = LoggerFactory.getLogger(CustomerService.class);

	@Autowired
	private IOrganisationRepository companyRepository;

	@Autowired
	RestUtils restUtils;

	@Autowired
	BeanConvertor beanConvertor;

	@Override
	public Organisation getCompanyByAccountNumber(String accountNumber) {
		logger.info("Inside getCompanyByAccountNumber service for account number: " + accountNumber);
		Organisation company = companyRepository.findBySalesForceAccountNumber(accountNumber);
		logger.info("Fetching organisation details " + company);
		return beanConvertor.convertOrganisation(company);
	}

	@Override
	public List<String> getCompanyByType(String type) {
		logger.info("Inside getCompanyByAccountNumber service for type: " + type);
		List<String> companies = companyRepository.findByCustomerName(OrganisationRole.getOrganisationRole(type));
		logger.info("Fetching organisation details " + companies);
		return companies;
	}

	@Override
	public Organisation getCompanyByUuid(String uuid) {
		logger.info("Inside getCompanyByAccountNumber service for uuid: " + uuid);
		Organisation company = companyRepository.findByUuid(uuid);
		logger.info("Fetching organisation details " + company);
		return company;
	}

	@Override
	public Organisation getCompanyById(Long id) {
		logger.info("Inside getCompanyByAccountNumber service for id: " + id);
		Organisation company = companyRepository.findById(id).get();
		logger.info("Fetching organisation details " + company);
		return beanConvertor.convertOrganisation(company);
	}

	@Override
	public Map<String, List<CustomerLocationPayload>> getCustomerAndLocationByUser(String userUuid) {
		return null;
	}

	@Override
	public CustomerWithLocation getCustomerWithLocationByUser(String userName) {
		User user = restUtils.getUserFromAuthService(userName);
		List<Organisation> organisation = companyRepository
				.getOrganisationAccessListByUuid(user.getOrganisation().getUuid());
		return beanConvertor.convertUserOrgToCustomerWithLocation(organisation);
	}

	@Override
	public Boolean resetCompanyData(String companyUuid) {
		return null;
	}

	@Override
	public List<Organisation> getListOfAllOrganisationByType(String type) {
		List<OrganisationRole> organisationTypeList = new ArrayList<>();
		if (type.equalsIgnoreCase("ALL")) {
			organisationTypeList.add(OrganisationRole.MANUFACTURER);
			organisationTypeList.add(OrganisationRole.END_CUSTOMER);
			organisationTypeList.add(OrganisationRole.INSTALLER);
			organisationTypeList.add(OrganisationRole.MAINTENANCE_MODE);
		} else if (type.equalsIgnoreCase("Manufacturer")) {
			organisationTypeList.add(OrganisationRole.MANUFACTURER);
		} else if (type.equalsIgnoreCase("Customer") || type.equalsIgnoreCase("end_customer")) {
			organisationTypeList.add(OrganisationRole.END_CUSTOMER);
		} else if (type.equalsIgnoreCase("Installer")) {
			organisationTypeList.add(OrganisationRole.INSTALLER);
//		} else if (type.equalsIgnoreCase("Pct")) {
//			organisationTypeList.add(OrganisationType.PCT);
		} else if (type.equalsIgnoreCase("Reseller")) {
			organisationTypeList.add(OrganisationRole.RESELLER);
		} else if (type.equalsIgnoreCase("Maintenance_mode")) {
			organisationTypeList.add(OrganisationRole.MAINTENANCE_MODE);
		}
		logger.info("Fetching User details");
		JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		logger.info("Username : " + jwtUser.getUsername());
		List<String> roleName = jwtUser.getRoleName();
		boolean roleAvailable = false;
		for (String rName : roleName) {
			if (rName.contains(AuthoritiesConstants.ROLE_CUSTOMER_ADMIN)
					|| rName.contains(AuthoritiesConstants.ROLE_ORGANIZATION_USER)) {
				roleAvailable = true;
				break;
			}
		}
		List<Organisation> companies = new ArrayList<>();
		logger.info("Inside getCompanyByAccountNumber service for type: " + type);
		if (roleAvailable && jwtUser.getAccountNumber() != null) {
			companies = companyRepository.findByListTypeAndAccountNumber(organisationTypeList, true,
					jwtUser.getAccountNumber());
		} else {
			companies = companyRepository.getOrganisation(organisationTypeList, true);
		}
		logger.info("Fetching organisation details " + companies);
		return beanConvertor.organisationsToOrganisationsConvertor(companies);
	}

	@Override
	public List<Organisation> getListOfCompanyByType(String type, String name) {
		logger.info("Inside getCompanyByAccountNumber service for type: " + type);
		logger.info("Fetching User details");
		JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		logger.info("Username : " + jwtUser.getUsername());
		List<String> roleName = jwtUser.getRoleName();
		boolean roleAvailable = false;
		for (String rName : roleName) {
			if (rName.contains(AuthoritiesConstants.ROLE_CUSTOMER_ADMIN)
					|| rName.contains(AuthoritiesConstants.ROLE_ORGANIZATION_USER)) {
				roleAvailable = true;
				break;
			}
		}
		List<Organisation> companies = new ArrayList<>();
		if (name != null && name.length() > 0) {
			companies = companyRepository.getListOfOrganisation(OrganisationRole.getOrganisationRole(type), name);
		} else {
			if (roleAvailable && jwtUser.getAccountNumber() != null) {
				companies = companyRepository.findByTypeAndAccountNumber(OrganisationRole.getOrganisationRole(type),
						jwtUser.getAccountNumber());
			} else {
				companies = companyRepository.findByType(OrganisationRole.getOrganisationRole(type));
			}
		}

		logger.info("Fetching organisation details successfully done");
		return beanConvertor.organisationsToOrganisationsConvertor(companies);
	}

	@Override
	public List<Organisation> findAllCustomer() {
		return companyRepository.findAllCustomer();
	}

	@Override
	public List<Organisation> getCustomerCompany() {
		List<Organisation> companies = companyRepository.findAllCustomer();
		return companies;
	}
	
	@Override
	public List<OrganisationDtoForInventory> getCustomerCompanyDto() {
		List<OrganisationDtoForInventory> companies = companyRepository.findAllCustomerDto();
		return companies;
	}
	
	

	@Override
	public Map<String, List<CustomerLocationPayload>> getAccessListByUserName(String userName) {
		logger.info("Inside getCustomerAndLocationByUser Method From Service");
		List<CustomerLocationPayload> customerList = null;
		Map<String, List<CustomerLocationPayload>> customerListMap = null;
		if (userName != null && !userName.isEmpty()) {
			logger.info(
					"Inside if userUuid != null && userId != 0 && userRepository.existsById(userId) of getCustomerAndLocationByUser Method From Service");
			User user = restUtils.getUserFromAuthService(userName);
			if (user.getOrganisation() != null) {
				Organisation organisation = companyRepository.findById(user.getOrganisation().getId()).get();
				List<Organisation> companies = null;

				if (organisation.getOrganisationRole().contains(OrganisationRole.INSTALLER)) {
					companies = organisation.getAccessList();
				}

				if (organisation.getOrganisationRole().contains(OrganisationRole.END_CUSTOMER)
						|| organisation.getOrganisationRole().contains(OrganisationRole.CUSTOMER)) {
					if (companies == null) {
						companies = new ArrayList<>();
					}
					companies.add(organisation);
				}

				if (organisation.getOrganisationRole().contains(OrganisationRole.MANUFACTURER)) {
					if (companies == null) {
						companies = new ArrayList<>();
						companies = companyRepository.findAllCustomer();
					} else {
						companies.addAll(companyRepository.findAllCustomer());
					}

				}

				if (companies != null && companies.size() > 0) {
					logger.info(
							"Inside if companies != null && companies.size() > 0 of getCustomerAndLocationByUser Method From Service");
					customerList = companies.stream().map(beanConvertor::convertCompanyToCustomerLocationPayload)
							.collect(Collectors.toList());
					customerListMap = new HashMap<String, List<CustomerLocationPayload>>();
					customerListMap.put("customers", customerList);
					logger.info("Exiting from getCustomerAndLocationByUser Method Of Service");
				} else {
					throw new OrganisationException("No customer found for user");
				}
			} else {
				logger.error(
						"Throwing exception of - Not valid user from getCustomerAndLocationByUser Method Of Service");
				throw new BadRequestException("Not valid user");
			}
		} else {
			logger.error("Throwing exception of - Not valid user from getCustomerAndLocationByUser Method Of Service");
			throw new BadRequestException("Not valid user");
		}
		return customerListMap;
	}
}
