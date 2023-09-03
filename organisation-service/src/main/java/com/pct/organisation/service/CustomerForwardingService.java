package com.pct.organisation.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.pct.common.model.CustomerForwardingRule;
import org.springframework.web.multipart.MultipartFile;

import com.pct.common.dto.CustomerForwardingGroupDTO;
import com.pct.common.dto.CustomerForwardingRuleDTO;
import com.pct.common.dto.CustomerForwardingRuleUrlDTO;
import com.pct.organisation.payload.CustomerForwardingRuleUrlPayload;

public interface CustomerForwardingService {

	List<CustomerForwardingGroupDTO> getAllCustomerForwardingGroup();
	
	List<CustomerForwardingGroupDTO> getCustomerForwardingGroupByOrganizationUuid(String OrganizationUuid);

	List<CustomerForwardingRuleDTO> getCustomerForwardingRulesByOrganizationUuids(Set<String> OrganizationUuids);
	
	List<CustomerForwardingRuleUrlDTO> getAllCustomerForwardingRuleUrl();
	
	CustomerForwardingRuleUrlDTO createCustomerForwardingRuleUrl(CustomerForwardingRuleUrlPayload payload);
	
	CustomerForwardingRuleUrlDTO modifyCustomerForwardingRuleUrl(String uuid, CustomerForwardingRuleUrlPayload payload);
	
	Map<String, Object> importCustomerForwardingRules(MultipartFile file);

	List<CustomerForwardingRuleDTO> getAllCustomerForwardingRules();
	
	List<String> getAllCustomerForwardingGroupName();

}
