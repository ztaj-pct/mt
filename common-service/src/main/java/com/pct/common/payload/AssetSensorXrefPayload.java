package com.pct.common.payload;

import java.time.Instant;

import com.pct.common.model.Asset;
import com.pct.common.model.Device;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Setter
@Getter
@NoArgsConstructor
public class AssetSensorXrefPayload {
	
	private Long id;
	
	private Asset asset;

	private Device device;
	
	private Boolean isActive;

	private Instant dateCreated;

	private Instant dateDeleted;	
	
	private Boolean isGatewayAttached;
	
	private String logUUId;

}
