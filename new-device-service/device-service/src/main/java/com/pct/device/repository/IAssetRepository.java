package com.pct.device.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pct.common.constant.AssetStatus;
import com.pct.common.dto.AssetDTO;
import com.pct.common.model.Asset;
import com.pct.device.dto.AssetListResponseDTO;
import com.pct.device.repository.projections.ApprovedAssetTypeCountView;
import com.pct.device.repository.projections.AssetGatewayView;

/**
 * @author Abhishek on 16/04/20
 */
@Repository
public interface IAssetRepository extends JpaRepository<Asset, Long>, JpaSpecificationExecutor<Asset> {

    @Query(nativeQuery = true, value = "SELECT category, count(*) as count FROM pct_device.asset where status = :status and account_number = :accountNumber group by category")
    List<ApprovedAssetTypeCountView> findAssetTypeAndCountByStatus(@Param("status") String status,
                                                                   @Param("accountNumber") String accountNumber);
    
    @Query("FROM Asset a WHERE a.assignedName = :assetId AND a.company.accountNumber= :accountNumber")
    Asset findAssetByAssetId(@Param("assetId") String assetId, @Param("accountNumber") String accountNumber);

    @Query("FROM Asset a WHERE a.company.accountNumber = :accountNumber AND a.vin LIKE %:vin and a.status='APPROVED'")
    Asset findAssetByVinNumber(@Param("vin") String vin, @Param("accountNumber") String accountNumber);

    @Query("FROM Asset a WHERE a.vin= :vin")
    List<Asset> findByVinNumber(@Param("vin") String vin);

    @Query("FROM Asset a WHERE a.vin= :vin AND a.company.accountNumber = :accountNumber")
    Asset findByVinAndAccountNumber(@Param("vin") String vin, @Param("accountNumber") String accountNumber);

    @Query("FROM Asset asset WHERE asset.company.accountNumber = :accountNumber AND asset.status = :status")
    List<Asset> findByAccountNumberAndStatus(@Param("accountNumber") String accountNumber, @Param("status") AssetStatus status);
    
    @Query("SELECT new com.pct.common.dto.AssetDTO(asset.id, asset.category, asset.status) FROM Asset asset WHERE asset.company.accountNumber = :accountNumber AND asset.status = :status")
    List<AssetDTO> findByAccountNumberAndStatusUsingDTO(@Param("accountNumber") String accountNumber, @Param("status") AssetStatus status);

    @Query("FROM Asset asset WHERE asset.company.accountNumber = :accountNumber")
    List<Asset> findByAccountNumber(@Param("accountNumber") String accountNumber);
    
    @Query("SELECT new com.pct.common.dto.AssetDTO(asset.id, asset.category, asset.status) FROM Asset asset WHERE asset.company.accountNumber = :accountNumber")
    List<AssetDTO> findByAccountNumberUsingDTO(@Param("accountNumber") String accountNumber);
    
    @Query("SELECT new com.pct.device.dto.AssetListResponseDTO(asset.uuid, asset.assignedName, asset.status, asset.category, asset.vin, asset.company.accountNumber, asset.manufacturerDetails, asset.gatewayEligibility, asset.createdOn, asset.updatedOn, asset.company.companyName, asset.isVinValidated, asset.comment, asset.year) FROM Asset asset WHERE asset.company.accountNumber =:accountNumber")
    List<AssetListResponseDTO> findAllForCustomDTO(@Param("accountNumber") String accountNumber);
    
    

    @Query("FROM Asset asset WHERE asset.uuid = :assetUuid")
    Asset findByUuid(@Param("assetUuid") String assetUuid);

    // installer admin apis

    @Query("From Asset a WHERE a.assignedName = :assetId AND a.company.id = :companyId")
    Asset findAssetByAssetIdAndCompany(@Param("assetId") String assetId,@Param("companyId") Long companyId);

    @Query("select count(*) from Asset a WHERE  a.company.id = :companyId")
    Long getAssetCountByCompanyId(@Param("companyId") Long companyId);

    @Query(nativeQuery = true, value = "SELECT a.*, g.imei, ih.date_ended FROM pct_device.asset a, " +
            "pct_device.gateway g, pct_installer_ms.install_history ih where a.account_number = :accountNumber " +
            "AND a.uuid = ih.asset_uuid AND g.uuid = ih.gateway_uuid")
    Page<AssetGatewayView> getAllAssetsByCompany(Pageable page, @Param("accountNumber") String accountNumber);
    
    @Query("FROM Asset a WHERE a.vin= :vin  AND a.id not in (:id)")
    Asset findByVinAndNotInId(@Param("vin") String vin, @Param("id") Long id);
    
    @Query("FROM Asset a WHERE a.assignedName = :assignedName AND a.company.accountNumber = :accountNumber")
    List<Asset> findByAssignedName(@Param("assignedName") String assignedName,@Param("accountNumber") String accountNumber);
    
    @Query("FROM Asset a WHERE a.assignedName= :assignedName  AND a.id not in (:id) AND a.company.accountNumber = :accountNumber")
    List<Asset> findByAssignedNameAndNotInId(@Param("assignedName") String assignedName, @Param("id") Long id,@Param("accountNumber") String accountNumber);

    @Query(value="select asset0.id as id, asset0.created_on as created_on, asset0.updated_on as updated_on, asset0.assigned_name as assigned_name, asset0.category as category, asset0.comment as comment, asset0.account_number as account_number, asset0.created_by as created_by, asset0.creation_method as creation_method, asset0.gateway_eligibility as gateway_eligibility, asset0.is_vin_validated as is_vin_validated, asset0.manufacturer_uuid as manufacturer_uuid, asset0.manufacturer_details_uuid as manufacturer_details_uuid, asset0.status as status, asset0.updated_by as updated_by, asset0.uuid as uuid, asset0.vin as vin, asset0.year as year from pct_device.asset asset0 cross join pct_company.company company1 where asset0.account_number=company1.account_number and company1.account_number = :account_number", nativeQuery=true)
    List<Object[]> getAssetDetailsByCompanyAccountNo(@Param("account_number") String account_number);
    
    @Query("SELECT asset.uuid FROM Asset asset WHERE asset.company.accountNumber = :accountNumber")
    List<String> findByCompanyAccountNo(@Param("accountNumber") String accountNumber);
}
