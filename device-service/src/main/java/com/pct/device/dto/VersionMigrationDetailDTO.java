package com.pct.device.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author prateek
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
