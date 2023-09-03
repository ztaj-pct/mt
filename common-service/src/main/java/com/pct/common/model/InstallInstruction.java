package com.pct.common.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import org.hibernate.envers.Audited;

import java.io.Serializable;

/**
 * @author Abhishek on 17/06/20
 */

@Entity
@Table(name = "install_instruction", catalog = "pct_installer_ms")
@Data
@NoArgsConstructor
public class InstallInstruction extends UserDateAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "instruction", columnDefinition = "VARCHAR(255)")
    private String instruction;

    @Column(name = "step_seq", columnDefinition = "INT(3)")
    private int stepSequence;

    @Column(name = "uuid", columnDefinition = "VARCHAR(255)")
    private String uuid;
}
