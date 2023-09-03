package com.pct.device.repository;

import com.pct.common.model.Asset;
import com.pct.common.model.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.time.Instant;
import java.util.List;

@Repository
public interface IAssetsRepository extends JpaRepository<Asset, Integer> {
	List<Asset> findByCompany(Company company);

	@Query("FROM Asset a WHERE a.company.id = :companyId")
	Page<Asset> getAllAssetsByCompany(Pageable page, @Param("companyId") Long companyId);

	@Query("From Asset a WHERE a.assignedName = :assetId AND a.company.id = :companyId")
	Asset findAssetByAssetIdAndCompany(@Param("assetId") String assetId, @Param("companyId") Long companyId);

	@Query("FROM Asset a where a.id IS NOT NULL")
	Page<Asset> getAllAsset(Pageable page);

	@Query("select count(*) from Asset a WHERE  a.company.id = :companyId")
	Long getAssetCountByCompanyId(@Param("companyId") Long comapnyId);

	@Query(value = "SELECT \n" + "    COUNT(ast.id) count,\n" + "    com.company_name companyName, com.id companyId ,\n"
			+ "    usr.first_name createdFirstName,\n" + "    usr.last_name createdLastName,\n" + "    usr1.first_name updatedFirstName,\n"
			+ "    usr1.last_name updatedLastName,\n" + "    DATE(ast.created_at)createdAt,\n"
			+ "    DATE(ast.updated_at) updatedAt\n" + "FROM\n" + "    company com,\n" + "    asset ast,\n"
			+ "    user usr,\n" + "    user usr1\n" + "WHERE\n" + "    com.id = ast.company_id\n"
			+ "        AND usr.email = ast.created_by\n" + "        AND usr1.email = ast.updated_by\n"
			+ "GROUP BY com.company_name , com.id,usr.first_name , usr.last_name , usr1.first_name , usr1.last_name , DATE(ast.created_at) , DATE(ast.updated_at)\n"
			+ "ORDER BY ?#{#pageable}", nativeQuery = true)
    Page<CustomerAssetsDTO> getAssetByCustomer(Pageable pageable);

	@Query(value = "SELECT \n" + "    COUNT(ast.id) count,\n" + "    com.company_name companyName, com.id companyId ,\n"
			+ "    usr.first_name createdFirstName,\n" + "    usr.last_name createdLastName,\n" + "    usr1.first_name updatedFirstName,\n"
			+ "    usr1.last_name updatedLastName,\n" + "    DATE(ast.created_at)createdAt,\n"
			+ "    DATE(ast.updated_at) updatedAt\n" + "FROM\n" + "    company com,\n" + "    asset ast,\n"
			+ "    user usr,\n" + "    user usr1\n" + "WHERE\n" + "    com.id = ast.company_id\n"
			+ "	   AND ast.company_id = ?#{#companyId}\n "
			+ "        AND usr.email = ast.created_by\n" + "        AND usr1.email = ast.updated_by\n"
			+ "GROUP BY com.company_name , com.id,usr.first_name , usr.last_name , usr1.first_name , usr1.last_name , DATE(ast.created_at) , DATE(ast.updated_at)\n"
			+ "ORDER BY ?#{#pageable}", nativeQuery = true)
    Page<CustomerAssetsDTO> getAssetByCustomerAndCompany(Pageable pageable, @Param("companyId") Long companyId);

	@Query("From Asset a where a.id in (:assetIds)")
	List<Asset> getAssetsByIds(@Param("assetIds") List<Long> assetIds);

	@Query("From Asset a where a.uuid in (:assetUuids)")
	List<Asset> getAssetsByUuids(@Param("assetUuids") List<String> assetUuids);

	interface CustomerAssetsDTO {

		String getCreatedFirstName();

		String getCreatedLastName();

		String getCompanyName();

		String getUpdatedFirstName();

		String getUpdatedLastName();

		BigInteger getCount();

		Instant getCreatedAt();

		Instant getUpdatedAt();

		Long getCompanyId();
	}

}
