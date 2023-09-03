package com.pct.organisation.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pct.common.constant.OrganisationRole;
import com.pct.common.dto.OrganisationDtoForInventory;
import com.pct.common.model.Organisation;
//import com.pct.common.model.OrganisationRole;
import com.pct.organisation.payload.OrganisationRequest;

@Repository
public interface IOrganisationRepository
		extends JpaRepository<Organisation, Long>, JpaSpecificationExecutor<Organisation> {
	
	
	@Query(nativeQuery = true, value = "select * FROM pct_organisation.organisation WHERE account_number = :accountNumber")
	public Organisation findBySalesForceAccountNumber(@Param("accountNumber") String accountNumber);

	@Query("FROM Organisation o join o.organisationRole orgRole WHERE orgRole = :role")
	List<Organisation> findByType(@Param("role") OrganisationRole role);
	
	@Query("FROM Organisation o join o.organisationRole orgRole WHERE orgRole = :role and o.accountNumber=:accountNumber")
	List<Organisation> findByTypeAndAccountNumber(@Param("role") OrganisationRole role,@Param("accountNumber") String accountNumber);
	
	@Query("Select distinct o From Organisation o join o.organisationRole orgRole where orgRole in :roles and o.isActive = :active and o.accountNumber=:accountNumber")
	List<Organisation> findByListTypeAndAccountNumber(@Param("roles") List<OrganisationRole> roles, @Param("active") Boolean active,@Param("accountNumber") String accountNumber);
	
	@Query("FROM Organisation o WHERE o.organisationName = :name")
	List<Organisation> findByOrganisationByOrganisationName(@Param("name") String name);
	
	@Query("select o.organisationName FROM Organisation o join o.organisationRole orgRole WHERE orgRole = :role")
	List<String> findByCustomerName(@Param("role") OrganisationRole role);
 
	@Query("Select distinct o From Organisation o join o.organisationRole orgRole where orgRole in :roles and o.isActive = :active")
	List<Organisation> getOrganisation(@Param("roles") List<OrganisationRole> roles, @Param("active") Boolean active);
	
	@Query("From Organisation o join o.organisationRole orgRole where orgRole = :role and o.organisationName like :name%")
	List<Organisation> getListOfOrganisation(@Param("role") OrganisationRole role, @Param("name") String name);

	@Query(value = "select * from organisation as o inner join organisation_role_mapping as orm on o.id = orm.organisation_id where orm.oraganisation_role = 'END_CUSTOMER' AND o.is_active = true", nativeQuery = true)
	List<Organisation> findAllCustomer();
	
	@Query("Select new com.pct.common.dto.OrganisationDtoForInventory(o.id,o.accountNumber,o.organisationName,o.isAssetListRequired,o.maintenanceMode) FROM Organisation o join o.organisationRole orgRole WHERE orgRole = 'END_CUSTOMER' or orgRole = 'CUSTOMER' ORDER BY o.organisationName ASC")
	List<OrganisationDtoForInventory> findAllCustomerDto();
	
//	@Query(" FROM Organisation o join o.organisationRole orgRole WHERE orgRole = 'END_CUSTOMER' ")
//	List<Organisation> getAllOrganisation1();

	@Query("FROM Organisation o WHERE o.uuid = :uuid")
	Organisation findByUuid(@Param("uuid") String uuid);

	@Query("FROM Organisation o WHERE o.uuid = :uuid or o.accountNumber =:accountNumber")
	Organisation findByUuidOrAccountNumber(@Param("uuid") String uuid, @Param("accountNumber") String accountNumber);
	
	@Query("Select distinct organisationName from Organisation")
	List<String> findDistinctOrganisationNames();
	
	@Query(" FROM Organisation o join o.organisationRole orgRole WHERE orgRole = 'END_CUSTOMER' ")
	List<Organisation> getAllOrganisation();

	@Query(" FROM Organisation o where o.organisationName in (:name)")
	List<Organisation> getAllOrganisationByName(@Param("name") List<String> name);
	
	@Query("Select o.accessList FROM Organisation o where o.uuid = :uuid")
	List<Organisation> getOrganisationAccessListByUuid(@Param("uuid") String uuid);
	
	@Query("select DISTINCT(o) From Organisation o join o.organisationRole orole where orole in (:orgRoles) and o.isActive = true ORDER BY o.organisationName ASC")
	List<Organisation> getAllActiveByOrganisationRolesIn(@Param("orgRoles") Set<OrganisationRole> organisationRoles);
	
	@Query("SELECT COUNT(o) > 0 FROM Organisation o WHERE o.organisationName = :name")
	boolean existByOrganisationName(@Param("name") String name);
	
	@Query("SELECT o FROM Organisation o left outer join o.accessList canView where o.id IS NOT NULL AND o.organisationRole in :types GROUP BY o.id")
	Page<Organisation> findAllCompanies(Pageable page, @Param("types") List<OrganisationRole> types);
	
	@Query("FROM Organisation c where c.id IS NOT NULL and c.id in :companyIds AND c.organisationRole in :types")
	Page<Organisation> findNonCustomerCompanyByIds(Pageable page, @Param("types") List<OrganisationRole> types,
											 @Param("companyIds") List<Long> companyIds);
	
	@Query("FROM Organisation c where c.id IS NOT NULL and c.id = :companyId AND c.organisationRole in :types")
	Page<Organisation> findNonCustomerCompanyById(Pageable page, @Param("types") List<OrganisationRole> types,
											 @Param("companyId") Long companyId);
	
	@Query("Select new com.pct.organisation.payload.OrganisationRequest(o.id,o.organisationName,o.uuid) FROM Organisation o join o.organisationRole orgRole WHERE orgRole = 'END_CUSTOMER' or orgRole = 'CUSTOMER' ORDER BY o.organisationName ASC")
	List<OrganisationRequest> getAllOrganisationFilter();
	
	
}
