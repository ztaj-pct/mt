package com.pct.organisation.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HubCustomerResponse {

	@JsonProperty("code")
    public String code;
    @JsonProperty("message")
    public Message message;
}
