package com.pct.organisation.payload;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Message {

	@JsonProperty("customer_list")
    public List<HubCustomer> customerList;
}
