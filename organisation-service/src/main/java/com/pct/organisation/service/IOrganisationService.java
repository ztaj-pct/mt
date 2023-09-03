package com.pct.organisation.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pct.common.constant.OrganisationRole;
import com.pct.common.model.Organisation;
import com.pct.common.util.Context;
import com.pct.organisation.dto.OrganisationDTO;
import com.pct.organisation.payload.AddOrganisationPayload;
import com.pct.organisation.payload.AddOrganisationResponse;
import com.pct.organisation.payload.CreateOrganisationPayload;
import com.pct.organisation.payload.OrganisationAccess;
import com.pct.organisation.payload.OrganisationAccessDTO;
import com.pct.organisation.payload.OrganisationAccessDTOForCAN;
import com.pct.organisation.payload.OrganisationListPayload;
import com.pct.organisation.payload.OrganisationPayload;
import com.pct.organisation.payload.OrganisationRequest;

public interface IOrganisationService {

	Organisation saveCustomer(CreateOrganisationPayload createOrganisationPayload, Context context);

	Organisation saveOrganisation(AddOrganisationPayload addOrganisationPayload, Context context) throws InstantiationException;

	AddOrganisationPayload update(AddOrganisationPayload addOrganisationPayload, Context context);

	void deleteByUuid(String uuid, Context context);

	Page<AddOrganisationResponse> findAllOrganisatons(Pageable pageable, OrganisationRole type, String accountNumber, String uuid,
			Context context);

	Organisation save(OrganisationAccess organisationPayload, Context context);

	OrganisationAccess update(OrganisationAccess organisations, Context context);

	List<OrganisationPayload> getCustomerOrganisationsFromHub(Context context);

	List<OrganisationPayload> getOrganisation(List<String> type, Boolean active, Context context);

	Page<OrganisationPayload> getAllAOrganisation(Map<String, String> filterValues, Pageable pageable, String type,
			String userName, Context context, String sort);

	Page<OrganisationAccessDTO> getAllActiveOrganisation(Pageable pageable, String type, Boolean status, Long userId,
			Context context);

	OrganisationAccessDTO getById(Long id, Context context);

	OrganisationAccessDTOForCAN getByCan(String accountNumber, Context context);

	void deleteById(Long id, Context context);

	List<String> findDistinctOrganisationNames(Context context);

	List<Organisation> findAllCustomer();

	Organisation getOrganisationByAccountNumber(String accountNumber, Context Context);

	List<Organisation> getOrganisationByType(String type, Context Context);
	
	public List<Organisation> getAllOrganisation(Context context);
	
	OrganisationDTO getOrganisationById(Long id);
	
	OrganisationDTO getOrganisationByAccountNumber(String id);
	
	List<Organisation> getListOfCompany(String userName);
	
	List<OrganisationListPayload> getAllCompany(String userName);
	
	OrganisationDTO getOrganisationByName(String name, Context context);

	List<String> getAllCompanyName();
	
	List<OrganisationDTO> getAllActiveByOrganisationRoles(List<String> organisationRoles, Context context);
	
	//public Boolean saveOrganisationDetail(AddOrganisationPayload addOrganisationPayload, Context context) throws InstantiationException ;
	
	Map<String, Integer> migrateOrganisationRole(Context context);
	
    List<OrganisationRequest>  getAllOrganisationFilter(Context context);
}
