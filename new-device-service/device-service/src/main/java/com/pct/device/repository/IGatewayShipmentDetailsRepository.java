package com.pct.device.repository;

import com.pct.device.model.GatewayShippingDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IGatewayShipmentDetailsRepository extends JpaRepository<GatewayShippingDetails, Long> {
    @Query("From GatewayShippingDetails gsd where gsd.company.accountNumber = :accountNumber")
    List<GatewayShippingDetails> findByAccountNumber(@Param("accountNumber") String accountNumber);
}
