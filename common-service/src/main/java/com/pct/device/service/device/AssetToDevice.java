package com.pct.device.service.device;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.sql.Timestamp;

/**
 * @author Abhishek on 20/01/21
 */

@Entity
@Getter
@Setter
@NoArgsConstructor
public class AssetToDevice {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long RECORD_ID;

    public String ASSET_ID;
    public String ASSET_TYPE;
    public String CUSTOMER;
    public String VIN;
    public String MAKE;
    public String MODEL;
    public String YEAR;
    public int MILES_BEFORE_INSTALL;
    public Timestamp INSTALL_TIMESTAMP;
    public String DEVICE_ID;
    public String COMMENT;
}
