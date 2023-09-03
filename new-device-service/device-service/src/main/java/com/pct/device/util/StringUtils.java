package com.pct.device.util;

public class StringUtils {

	public static String getContainsLikePattern(String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) {
            return "%";
        }
        else {
            return "%" + searchTerm.toLowerCase() + "%";
        }
    }
	
	public static String replaceBlankSpaceWithUnderscore(String originalString) {
		return originalString.replaceAll("\\s+", "_");
	}
}
