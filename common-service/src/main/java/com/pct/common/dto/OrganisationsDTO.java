package com.pct.common.dto;

import com.pct.common.model.Organisation;

public class OrganisationsDTO {

    private Long id;

    private String organisationName;
    private String shortName;

    private String type;

    private boolean status;

    private String email;

    public OrganisationsDTO() {
    }

    public OrganisationsDTO(Long id, String organisationName, String shortName, String type, boolean status, String email) {
        super();
        this.id = id;
        this.organisationName = organisationName;
        this.shortName = shortName;
        this.type = type;
        this.status = status;
        this.email = email;
    }

    public OrganisationsDTO(Organisation organisations) {
        this.id = organisations.getId();
        this.organisationName = organisations.getOrganisationName();
        this.status = organisations.getIsActive();
        // this.email = companies.getEmail();
    }

    public static boolean isValid(OrganisationsDTO organisations) {
        if (organisations.getType() != null) {
            if (organisations.getType().equals("Fleet")) {
                return (organisations.getOrganisationName() != null && organisations.getEmail() != null
                        && organisations.getType() != null && organisations.getShortName() != null && organisations.isStatus());
            }
            return (organisations.getOrganisationName() != null && organisations.getEmail() != null && organisations.getType() != null
                    && organisations.getShortName() != null && organisations.isStatus());

        }
        return false;

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrganisationName() {
        return organisationName;
    }

    public void setOrganisationName(String organisationName) {
        this.organisationName = organisationName;
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
