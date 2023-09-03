package com.pct.device.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pct.common.constant.AssetStatus;
import com.pct.common.dto.AssetDTO;
import com.pct.common.model.Asset;

@Repository
public interface IAssetRepository extends JpaRepository<Asset, Long>, JpaSpecificationExecutor<Asset> {

	@Query("FROM Asset asset WHERE asset.uuid = :assetUuid")
	Asset findByUuid(@Param("assetUuid") String assetUuid);
	
//	@Query("FROM Asset asset WHERE asset.name = :assetName")
//	AssetToDevice findByAssetID(@Param("assetName") String assetName);

	@Query("FROM Asset a WHERE a.assignedName = :assignedName AND a.organisation.accountNumber = :accountNumber")
	List<Asset> findByAssignedName(@Param("assignedName") String assignedName,
			@Param("accountNumber") String accountNumber);

	@Query("From Asset a WHERE a.assignedName = :assetId AND a.organisation.id = :companyId")
	Asset findAssetByAssetIdAndOrganisation(@Param("assetId") String assetId, @Param("companyId") Long companyId);

	@Query("FROM Asset a WHERE a.assignedName= :assignedName  AND a.id not in (:id) AND a.organisation.accountNumber = :accountNumber")
	List<Asset> findByAssignedNameAndNotInId(@Param("assignedName") String assignedName, @Param("id") Long id,
			@Param("accountNumber") String accountNumber);

	@Query("FROM Asset a WHERE a.vin= :vin")
	List<Asset> findByVinNumber(@Param("vin") String vin);

	@Query("FROM Asset a WHERE a.vin= :vin  AND a.id not in (:id)")
	Asset findByVinAndNotInId(@Param("vin") String vin, @Param("id") Long id);

	@Query("FROM Asset a WHERE a.assignedName = :assignedName")
	public Asset findByAssignedName(@Param("assignedName") String assignedName);
	
	@Query("FROM Asset a WHERE a.assignedName like %:assignedName%")
	public List<Asset> findAssetByAssignedName(@Param("assignedName") String assignedName);

	public Asset findByVin(String vin);

	@Query("From Asset a where a.uuid in (:assetUuids)")
	List<Asset> getAssetsByUuids(@Param("assetUuids") List<String> assetUuids);

	@Query("select count(*) from Asset a WHERE  a.organisation.id = :companyId")
	Long getAssetCountByCompanyId(@Param("companyId") Long companyId);

	@Query("From Asset a WHERE a.assignedName = :assetId AND a.organisation.id = :companyId")
	Asset findAssetByAssetIdAndCompany(@Param("assetId") String assetId, @Param("companyId") Long companyId);
	
	@Query("From Asset a where a.assignedName in (:assignedName)")
	List<Asset> getAssetsByAssignedName(@Param("assignedName") Set<String> assignedName);
	
	@Query("Select a From Asset a WHERE a.assignedName = :assetId")
	Asset findByAssetId(@Param("assetId") String assetId);
	 @Query("FROM Asset a WHERE a.vin= :vin AND a.organisation.accountNumber = :accountNumber")
	 Asset findByVinAndAccountNumber(@Param("vin") String vin, @Param("accountNumber") String accountNumber);
  @Query("FROM Asset a WHERE a.vin= :vin")
	    Asset findByVinAndAccountNumber(@Param("vin") String vin);

//	 @Query("FROM Asset a WHERE a.status= :assetStatus AND a.organisation.accountNumber = :accountNumber")
//	 Asset findByAccountNumberAndStatusUsingDTO(String accountNumber, AssetStatus assetStatus);
	 
	    @Query("SELECT new com.pct.common.dto.AssetDTO(asset.id, asset.category, asset.status) FROM Asset asset WHERE asset.organisation.accountNumber = :accountNumber AND asset.status = :status")
	    List<AssetDTO> findByAccountNumberAndStatusUsingDTO(@Param("accountNumber") String accountNumber, @Param("status") AssetStatus status);
	    
	    @Query("SELECT new com.pct.common.dto.AssetDTO(asset.id, asset.category, asset.status) FROM Asset asset WHERE asset.organisation.accountNumber = :accountNumber")
	    List<AssetDTO> findByAccountNumberUsingDTO(@Param("accountNumber") String accountNumber);
}
