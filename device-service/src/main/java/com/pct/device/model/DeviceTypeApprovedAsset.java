package com.pct.device.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;


@Entity
@Table(name = "device_type_approved_asset", catalog = "pct_device")
@Data
@NoArgsConstructor
public class DeviceTypeApprovedAsset implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_product_name", columnDefinition = "VARCHAR(255)")
    private String deviceProductName;

    @Column(name = "device_product_code", columnDefinition = "VARCHAR(255)")
    private String deviceProductCode;

    @Column(name = "asset_type", columnDefinition = "VARCHAR(255)")
    private String assetType;
}
