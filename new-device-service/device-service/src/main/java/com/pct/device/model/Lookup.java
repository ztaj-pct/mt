package com.pct.device.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "lookup", catalog = "pct_device")
@Getter
@Setter
@NoArgsConstructor
public class Lookup implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "field")
    private String field;

    @Column(name = "value")
    private String value;

    @Column(name = "display_label")
    private String displayLabel;
}

