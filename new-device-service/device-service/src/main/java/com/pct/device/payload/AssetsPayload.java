package com.pct.device.payload;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.pct.common.constant.AssetStatus;
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AssetsPayload {

    private Long id;
    private String uuid;
    private String assignedName;
    private String eligibleGateway;
    private String vin;
    private String category;
    private String year;
    private String manufacturer;
    private CompanyPayload company;
    private String status;
    private Boolean isVinValidated;
    private String comment;
    //private Set<SensorPayload> sensor = new HashSet<>();

    public String getAssignedName() {
        return assignedName;
    }

    public void setAssignedName(String assignedName) {
        this.assignedName = assignedName;
    }

    public String getEligibleGateway() {
        return eligibleGateway;
    }

    public void setEligibleGateway(String eligibleGateway) {
        this.eligibleGateway = eligibleGateway;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String assetType1) {
        this.category = assetType1;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String modelYear) {
        this.year = modelYear;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public CompanyPayload getCompany() {
		return company;
	}

	public void setCompany(CompanyPayload company) {
		this.company = company;
	}

    public Boolean getVinValidated() {
        return isVinValidated;
    }

    public void setVinValidated(Boolean vinValidated) {
        isVinValidated = vinValidated;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    /*
     * public Set<SensorPayload> getSensor() { return sensor; }
     *
     * public void setSensor(Set<SensorPayload> sensor) { this.sensor = sensor; }
     */
}
