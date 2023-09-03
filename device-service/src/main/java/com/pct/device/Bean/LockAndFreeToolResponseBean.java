package com.pct.device.Bean;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class LockAndFreeToolResponseBean {
	
	
	private Long id;
	
	private String uuid;
	
	private List<LockAndFreeToolDataResponseBean> lockList;

	@Override
	public String toString() {
		return "LockAndFreeToolResponseBean [id=" + id + ", uuid=" + uuid + ", lockList=" + lockList + "]";
	}

    
}
