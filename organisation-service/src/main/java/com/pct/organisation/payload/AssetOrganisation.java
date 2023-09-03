package com.pct.organisation.payload;

import java.util.List;

import com.pct.common.model.Organisation;

public class AssetOrganisation {

	Organisation organisation;
    List<AssetPayload> assetList;
    Boolean overwriteFlag;

    public Organisation getOrganisation() {
        return organisation;
    }

    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }

    public List<AssetPayload> getAssetList() {
        return assetList;
    }

    public void setAssetList(List<AssetPayload> assetList) {
        this.assetList = assetList;
    }

    public Boolean getOverwriteFlag() {
        return overwriteFlag;
    }

    public void setOverwriteFlag(Boolean overwriteFlag) {
        this.overwriteFlag = overwriteFlag;
    }
}
