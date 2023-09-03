package com.pct.device.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author Abhishek on 02/07/20
 */

@Entity
@Table(name = "gateway_type_approved_asset", catalog = "pct_device")
@Data
@NoArgsConstructor
public class GatewayTypeApprovedAsset implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "gateway_product_name", columnDefinition = "VARCHAR(255)")
    private String gatewayProductName;

    @Column(name = "gateway_product_code", columnDefinition = "VARCHAR(255)")
    private String gatewayProductCode;

    @Column(name = "asset_type", columnDefinition = "VARCHAR(255)")
    private String assetType;
}
