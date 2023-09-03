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
 * @author Abhishek on 21/01/21
 */

@Entity
@Getter
@Setter
@NoArgsConstructor
public class AssetToDeviceHistory {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long RECORD_ID;

    public String CUSTOMER;
    public String ASSET_ID;
    public Timestamp INSTALL_TIMESTAMP;
    public Timestamp UNINSTALL_TIMESTAMP;
    public String DEVICE_ID;
}
