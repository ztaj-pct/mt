package com.pct.common.payload;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AttributeRequest {

	

	@JsonProperty("product_uuid")
	private String productUuid;

	@JsonProperty("attributes")
	private List<AttributeDetails> attributeDetails;

	@JsonProperty("attribute-name")
	private String attributeName;

	@JsonProperty("attribute-value")
	private String attributeValue;


}
