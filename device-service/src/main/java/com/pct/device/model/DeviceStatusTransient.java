package com.pct.device.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

import javax.persistence.*;

import com.pct.common.constant.DeviceStatus;


@Entity
@Table(name = "device_status_transient", catalog = "pct_device")
@Getter
@Setter
@NoArgsConstructor
public class DeviceStatusTransient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id")
    private String deviceId;

	@Column(name = "status")
	@Enumerated(value = EnumType.STRING)
	private DeviceStatus status;

    @Column(name = "date_created")
    private Instant date_created;

}
