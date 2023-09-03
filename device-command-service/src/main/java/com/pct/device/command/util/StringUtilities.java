package com.pct.device.command.util;

import java.nio.ByteOrder;

public class StringUtilities {
	public static final int INT_SIZE = Integer.SIZE / 8;

	public static String Byte2HexString(byte[] b) {
		return Byte2HexString(b, 0, b.length);
	}


    /**
     * Convert Hex string to Byte array
     * @param str - String for source
     * @return - byte[] for result
     */
    public static byte[] Hex2Byte(String str) {
        return Hex2Byte(str, 0, str.length());
    }

    /**
     * Convert Hex sub-string to byte array
     * @param str - String for source
     * @param offset - int for starting location of substring
     * @param len - int for length of substring
     * @return - byte[] for result
     */
    public static byte[] Hex2Byte(String str, int offset, int len)
    {
        byte[] bytes = new byte[len / 2];
        for (int i = offset, j=0; i < (offset + bytes.length); i++, j++) {
            bytes[j] = (byte) Integer.parseInt(str.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }

	/**
	 * Convert section of a byte array into hex string
	 * 
	 * @param b      - array of bytes for source
	 * @param offset - int for offset into the array
	 * @param len    - int for length of the array section
	 * @return - String for result
	 */
	public static String Byte2HexString(byte[] b, int offset, int len) {

		// String Buffer can be used instead

		String hexString = "";
		String hexDigits = "";

		for (int n = offset; n < (offset + len); n++) {
			hexDigits = (Integer.toHexString(b[n] & 0XFF));

			if (hexDigits.length() == 1) {
				hexString = hexString + "0" + hexDigits;
			} else {
				hexString = hexString + hexDigits;
			}

			if (n < (offset + len) - 1) {
				hexString = hexString + "";
			}
		}

		return hexString;
	}

	public static int parseInt(byte[] src, int startIndex, int length, ByteOrder byteOrder) {
		return parseInt(src, startIndex, length, byteOrder, null);
	}

	/**
	 * Convert array of bytes to an integer. For array of 2 bytes the result is
	 * signed. for 1 byte it is unsigned.
	 * 
	 * @param src        - byte array to be parsed
	 * @param startIndex - int for offset into the byte array
	 * @param length     - int for number of bytes to parse
	 * @param byteOrder  - ByteOrder object
	 * @param error      - arry of boolean with allocation for at least one member
	 *                   for error return instead of using exception
	 * @return - iny with the parsed value or 0 if the input array is null/empty or
	 *         the input length is wrong
	 */
	public static int parseInt(byte[] src, int startIndex, int length, ByteOrder byteOrder, boolean[] error) {

		// Checks and restrictions
		//////////////////////////
		if ((src == null) || (src.length == 0) || (length > src.length) || (length > INT_SIZE)) {
			if ((error != null) && (error.length > 0)) {
				error[0] = true;
			}
			return 0;
		}

		// Now convert
		//////////////
		int result = 0;
		int shift = 0;
		if (byteOrder.equals(ByteOrder.BIG_ENDIAN)) {
			for (int i = (startIndex + length - 1); i >= startIndex; i--) {
				result |= ((src[i] & 0xff) << shift);
				shift += 8;
			}
		} else {
			for (int i = startIndex; i <= (startIndex + length - 1); i++) {
				result |= ((src[i] & 0xff) << shift);
				shift += 8;
			}
		}
		if ((error != null) && (error.length > 0)) {
			error[0] = false;
		}
		return result;
	}
	
	public static String hexToAscii(String hexStr) {
        StringBuilder output = new StringBuilder("");

        for (int i = 0; i < hexStr.length(); i += 2) {
            String str = hexStr.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }

        return output.toString();
    }

}
