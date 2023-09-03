package com.pct.device.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class ImportForwardingResponseDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private ImportForwardingDTO importData;

	private boolean status;

	private String message;

}
