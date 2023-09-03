package com.pct.device.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "column_defs", catalog = "pct_device")
@Getter
@Setter
public class ColumnDefs {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	 @Lob
	 @Column(name = "report_columndefs")
	 private String reportColumnDefs;

}
