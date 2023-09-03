package com.pct.device.model;

import com.pct.common.model.Company;
import com.pct.common.model.Gateway;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "gateway_shipping_details", catalog = "pct_device")
@Getter
@Setter
@NoArgsConstructor
public class GatewayShippingDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "gateway_uuid", referencedColumnName = "uuid")
    private Gateway gateway;

    @ManyToOne
    @JoinColumn(name = "account_number", referencedColumnName = "account_number")
    private Company company;

    @Column(name = "date_shipped")
    private Instant dateShipped;

    @Column(name = "address_shipped")
    private String addressShipped;

}
