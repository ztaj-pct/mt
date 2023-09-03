package com.pct.device.payload;

import com.pct.common.model.Company;

import java.util.List;

public class CompanyAccessDTO {

    private CompanyPayload customer;
    private List<Company> companyViewList;
    private boolean status;
    private String type;

    public CompanyPayload getCustomer() {
        return customer;
    }

    public void setCustomer(CompanyPayload customer) {
        this.customer = customer;
    }

    public List<Company> getCompanyViewList() {
        return companyViewList;
    }

    public void setCompanyViewList(List<Company> companyViewList) {
        this.companyViewList = companyViewList;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


}
