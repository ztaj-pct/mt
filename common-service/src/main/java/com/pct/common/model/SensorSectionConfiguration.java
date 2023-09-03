package com.pct.common.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sensor_section_configuration", catalog = "pct_device")
@Getter
@Setter
@NoArgsConstructor
public class SensorSectionConfiguration extends UserDateAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_code", columnDefinition = "VARCHAR(255)")
    private String productCode;

    @Column(name = "product_name", columnDefinition = "VARCHAR(255)", nullable = false)
    private String productName;

    @Column(name = "uuid")
    private String uuid;

    @Column(name = "section_uuid")
    private String section;
    
}
