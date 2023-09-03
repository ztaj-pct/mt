package com.pct.organisation.payload;

public class AssetPayload {

	 private Long id;
	    private String assetId;
	    private String compatibleType;
	    private String vin;
	    private String assetType;
	    private String year;
	    private String manufacturer;
	    //private Set<SensorPayload> sensor = new HashSet<>();


	    public String getAssetId() {
	        return assetId;
	    }

	    public void setAssetId(String assetId) {
	        this.assetId = assetId;
	    }

	    public String getCompatibleType() {
	        return compatibleType;
	    }

	    public void setCompatibleType(String isProductIsApprovedForAsset) {
	        this.compatibleType = isProductIsApprovedForAsset;
	    }

	    public String getVin() {
	        return vin;
	    }

	    public void setVin(String vin) {
	        this.vin = vin;
	    }

	    public String getAssetType() {
	        return assetType;
	    }

	    public void setAssetType(String assetType1) {
	        this.assetType = assetType1;
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

	    /*
	     * public Set<SensorPayload> getSensor() { return sensor; }
	     *
	     * public void setSensor(Set<SensorPayload> sensor) { this.sensor = sensor; }
	     */
}
