package com.pct.installer.entity;

import java.io.Serializable;
import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.springframework.data.annotation.CreatedDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pct.common.model.Device;
import com.pct.common.model.InstallHistory;
import com.pct.common.model.ReasonCode;
import com.pct.common.model.User;
import com.pct.installer.constant.LogIssueStatus;

import lombok.Data;

/**
 * @author Abhishek on 11/06/20
 */

@Entity
@Table(name = "log_issue", catalog = "pct_installer_ms")
@Data
public class LogIssue implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "install_code", referencedColumnName = "install_code")
    private InstallHistory installHistory;

    @ManyToOne()
    @JoinColumn(name = "sensor_uuid", referencedColumnName = "uuid")
    private Device device;
    
    @ManyToOne
    @JoinColumn(name = "gateway_uuid", referencedColumnName = "uuid")
    private Device gateway;

    @ManyToOne
    @JoinColumn(name = "reason_code_uuid", referencedColumnName = "uuid")
    private ReasonCode reasonCode;

    @Column(name = "issue_type", columnDefinition = "VARCHAR(255)")
    private String issueType;

    @Column(name = "related_uuid", columnDefinition = "VARCHAR(255)")
    private String relatedUuid;
    
    @Column(name = "comment", columnDefinition = "VARCHAR(255)")
    private String comment;

    @Column(name = "data", columnDefinition = "VARCHAR(512)")
    private String data;

    @Column(name = "status", columnDefinition = "VARCHAR(255)")
    @Enumerated(value = EnumType.STRING)
    private LogIssueStatus status;
    
    @Column(name = "uuid")
    private String uuid;
    
    @CreatedDate
    private Instant createdOn;
 

    @JsonIgnoreProperties("role")
    @ManyToOne
    @JoinColumn(name = "created_by", referencedColumnName = "uuid")
    private User createdBy;

    @JsonIgnoreProperties("role")
    @ManyToOne
    @JoinColumn(name = "updated_by", referencedColumnName = "uuid")
    private User updatedBy;
    
    @Column(name = "sensor_id")
    private String sensorId;
    
    @Column(name = "type")
    private String type;
    
    
}
