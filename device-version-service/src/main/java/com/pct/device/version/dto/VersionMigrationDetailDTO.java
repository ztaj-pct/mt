package com.pct.device.version.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.pct.device.version.payload.PackagePayload;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author dhruv
 *
 */
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Setter
@Getter
@NoArgsConstructor
public class VersionMigrationDetailDTO {

	private Long stepId;
	private PackagePayload fromPackage;
    private PackagePayload toPackage;
    private String atCommand;
    private Long stepOrderNumber;

}
