package com.pct.device.repository;

import com.pct.common.model.AttributeValue;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IAttributeValueRepository extends JpaRepository<AttributeValue, Long> {

    /*@Query("FROM AttributeValue attributeValue WHERE attributeValue.product_uuid = :productUuid")
    List<AttributeValue> findByProductUuid(@Param("productUuid") String productUuid);
*/
    @Query("FROM AttributeValue attributeValue WHERE attributeValue.uuid = :uuid")
    AttributeValue findByUuid(@Param("uuid") String uuid);

//    @Query("FROM AttributeValue attributeValue WHERE attributeValue.attributeUUID = :attributeUuid AND attributeValue.relatedUUID = :relatedUuid")
//    AttributeValue findByAttributeUuidAndGatewayUuid(String attributeUuid, String relatedUuid);

    @Query("FROM AttributeValue attributeValue WHERE attributeValue.gateway.imei = :deviceId")
    List<AttributeValue> findByDeviceImei(@Param("deviceId") String deviceId);
    
    @Query("FROM AttributeValue attributeValue WHERE attributeValue.gateway.macAddress = :deviceId")
    List<AttributeValue> findByDeviceMacAddress(@Param("deviceId") String deviceId);
    
}
