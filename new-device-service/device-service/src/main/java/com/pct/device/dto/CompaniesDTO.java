package com.pct.device.dto;

import com.pct.common.model.Company;

public class CompaniesDTO {

    private Long id;

    private String companyName;
    private String shortName;

    private String type;

    private boolean status;

    private String email;

    public CompaniesDTO() {
    }

    public CompaniesDTO(Long id, String companyName, String shortName, String type, boolean status, String email) {
        super();
        this.id = id;
        this.companyName = companyName;
        this.shortName = shortName;
        this.type = type;
        this.status = status;
        this.email = email;
    }

    public CompaniesDTO(Company companies) {
        this.id = companies.getId();
        this.companyName = companies.getCompanyName();
        this.status = companies.getIsActive();
        // this.email = companies.getEmail();
    }

    public static boolean isValid(CompaniesDTO companies) {
        if (companies.getType() != null) {
            if (companies.getType().equals("Fleet")) {
                return (companies.getCompanyName() != null && companies.getEmail() != null
                        && companies.getType() != null && companies.getShortName() != null && companies.isStatus());
            }
            return (companies.getCompanyName() != null && companies.getEmail() != null && companies.getType() != null
                    && companies.getShortName() != null && companies.isStatus());

        }
        return false;

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
