package com.pct.common.model;

import lombok.NoArgsConstructor;

import javax.persistence.*;

import org.hibernate.envers.Audited;

import java.io.Serializable;

@Entity
@Table(name = "reason_code", catalog = "pct_installer_ms")
@NoArgsConstructor
public class ReasonCode extends UserDateAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", columnDefinition = "VARCHAR(255)")
    private String code;

    @Column(name = "value", columnDefinition = "VARCHAR(255)")
    private String value;

    @Column(name = "issue_type", columnDefinition = "VARCHAR(255)")
    private String issueType;

    @Column(name = "uuid", columnDefinition = "VARCHAR(255)")
    private String uuid;

    public ReasonCode(String issueType) {
        this.issueType = issueType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getIssueType() {
        return issueType;
    }

    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }
}
