package com.pct.device.Bean;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class ProductionToolFetchBean {
	 private String startDate;
	 private String endDate;
	 private String toolName;
	 private Integer _page;
	 private Integer _limit;
	 private String _sort;
	 private String _order;
	 private String queryParam;
	 private String queryParamValue;
	 
	@Override
	public String toString() {
		return "ProductionToolFetchBean [startDate=" + startDate + ", endDate=" + endDate + ", toolName=" + toolName
				+ ", _page=" + _page + ", _limit=" + _limit + ", _sort=" + _sort + ", _order=" + _order + "]";
	}
	 
	 
    
}
