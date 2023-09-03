package com.pct.device.version.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.pct.common.model.Organisation;

@Repository
public interface IOrganisationVersionRepository
		extends JpaRepository<Organisation, Long>, JpaSpecificationExecutor<Organisation> {
	
	 
	//@Query("select o.organisationName from Organisation as o inner join OrganisationSettings as os on os.organisationUuid = o.uuid where os.fieldName = 'isApprovalReqForDeviceUpdate' and os.fieldValue = 'false' and o.type = 'CUSTOMER'")
	@Query("select o.organisationName from Organisation as o inner join OrganisationSettings as os on os.organisationUuid = o.uuid where os.fieldName = 'isApprovalReqForDeviceUpdate' and os.fieldValue = 'false'")
	List<String> findOrganisationsByFirmwareFlag();

}
