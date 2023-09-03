package com.pct.device.repository.projections;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pct.common.constant.AssetCategory;
import com.pct.common.constant.AssetStatus;
import com.pct.common.model.Company;
import com.pct.common.model.Manufacturer;
import com.pct.common.model.ManufacturerDetails;
import com.pct.common.model.User;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.time.Instant;

/**
 * @author Abhishek on 24/02/21
 */
public interface AssetGatewayView {

    Long getId();
    String getAssigned_name();
    String getGateway_eligibility();
    String getAccount_number();
    String getVin();
    String getUuid();
    String getYear();
    String getManufacturer_uuid();
    AssetCategory getCategory();
    AssetStatus getStatus();
    String getManufacturer_details_uuid();
    String getCreated_by();
    String getUpdated_by();
    Instant getCreated_on();
    Instant getUpdated_on();
    String getComment();
    Boolean getIs_vin_validated();
    String getImei();
    String getDate_ended();
}
