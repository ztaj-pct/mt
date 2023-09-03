package com.pct.device.service;

import javax.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pct.device.Bean.ProductionToolBean;
import com.pct.device.Bean.ProductionToolResponseBean;

public interface IProductionToolService {

	public void saveProductionToolData(@Valid ProductionToolBean productionToolBean, String msgUuid);

	public Page<ProductionToolResponseBean> getProductionToolDataPagination(String startDate, String endDate, String toolName,
			Pageable pageable, String msgUuid, String queryParam, String queryParamValue);

}
