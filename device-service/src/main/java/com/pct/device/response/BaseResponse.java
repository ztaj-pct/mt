package com.pct.device.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Instantiates a new base response.
 */

/**
 * Instantiates a new base response.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BaseResponse<T, ID> extends GenericResponse{
	
	/** The data. */
	T data;
	
	/** The id. */
	ID id;

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public ID getId() {
		return id;
	}

	public void setId(ID id) {
		this.id = id;
	}
	
	
	

}
