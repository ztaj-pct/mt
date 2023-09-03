package com.pct.device.version.payload;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WaterfallInfo {
	private List<Integer> configId;
    private List<String> configCRC;
    private List<String> configIdentificationVersion;
    private int numberOfFiles;
    
}
