package com.pct.common.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sensor_reason_code", catalog = "pct_installer_ms")
@Data
@NoArgsConstructor
public class SensorReasonCode implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sensor_product_name", columnDefinition = "VARCHAR(255)")
    private String sensorProductName;

    @ManyToOne
    @JoinColumn(name = "reason_code_uuid", referencedColumnName = "uuid")
    private ReasonCode reasonCode;
    
    @Column(name = "sensor_product_code", columnDefinition = "VARCHAR(255)")
    private String sensorProductCode;
    
    
}
