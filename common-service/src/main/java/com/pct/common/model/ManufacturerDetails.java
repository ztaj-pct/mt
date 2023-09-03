package com.pct.common.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

import org.hibernate.envers.Audited;

import java.io.Serializable;

@Entity
@Table(name = "manufacturer_details", catalog = "pct_device")
@Getter
@Setter
@NoArgsConstructor
public class ManufacturerDetails extends UserDateAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "model")
    private String model;

    @Column(name = "config")
    private String config;

    @Column(name = "uuid")
    private String uuid;

    @ManyToOne
    @JoinColumn(name = "manufacturer_uuid", referencedColumnName = "uuid")
    private Manufacturer manufacturer;
}
