package com.pct.device.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

import com.pct.common.model.Device;
import com.pct.common.model.Organisation;

import java.time.Instant;

@Entity
@Table(name = "device_shipping_details", catalog = "pct_device")
@Getter
@Setter
@NoArgsConstructor
public class DeviceShippingDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "gateway_uuid", referencedColumnName = "uuid")
    private Device device;

    @ManyToOne
    @JoinColumn(name = "account_number", referencedColumnName = "account_number")
    private Organisation company;

    @Column(name = "date_shipped")
    private Instant dateShipped;

    @Column(name = "address_shipped")
    private String addressShipped;

}
