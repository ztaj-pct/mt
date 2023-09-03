package com.pct.device.payload;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AssetCompany {
	CompanyPayload company;
    List<AssetsPayload> assetList;
    Boolean overwriteFlag;

   

    public List<AssetsPayload> getAssetList() {
        return assetList;
    }

    public void setAssetList(List<AssetsPayload> assetList) {
        this.assetList = assetList;
    }

    public Boolean getOverwriteFlag() {
        return overwriteFlag;
    }

    public void setOverwriteFlag(Boolean overwriteFlag) {
        this.overwriteFlag = overwriteFlag;
    }

	public CompanyPayload getCompany() {
		return company;
	}

	public void setCompany(CompanyPayload company) {
		this.company = company;
	}

}
