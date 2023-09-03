package com.pct.device.util;

import org.springframework.lang.Nullable;

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
	
	public static boolean isEmpty(@Nullable Object str) {
		return (str == null || "".equals(str));
	}
}
