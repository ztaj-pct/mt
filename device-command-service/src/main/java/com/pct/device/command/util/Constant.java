package com.pct.device.command.util;

public class Constant {

	// used for changing AT-Command to Datagram
	public static final byte UDP_RESPONSE_IDENTIFIER_BYTE = '%';
	public static final int UDP_RESPONSE_IDENTIFIER_LEN = 1;
	public static final byte UDP_RESPONSE_DELIMITER_BYTE = '^';
	public static final byte UDP_COMMAND_IP_DELIMITER_BYTE = '&';
	public static final int UDP_RESPONSE_DELIMITER_LEN = 1;
	public static final byte UDP_COMMAND_END_BYTE = '~';
	public static final int UDP_COMMAND_END_BYTE_LEN = 1;
	public static final int TIMESTAMP_BYTES_LEN = 16; // long integer converted to ascii
	public static final int RECEIVE_PACKET_LEN = 512;
	public static final String ERROR_PREFIX = "ERROR:";
	public static final String SOCKET_EXCEPTION = ERROR_PREFIX + " Socket Exception!";
	public static final String NON_VALID_IP = ERROR_PREFIX + " Non valid device IP address!";
	public static final String UNKNOWN_DEVICE_HOST = ERROR_PREFIX + " Unknown device host!";
	public static final int MAX_IDENTIFIER_TOTAL_LENGTH = 63;
	public static final String SENDING_FAILED = ERROR_PREFIX + " Sending UDP packet failed!";

	public static final int FLEET_COMPLETE_VPN_PORT = 15800;

	// For Ack/Neck
	public static final int FRAME_CODE_7E = 0x7E;
	public static final int FRAME_CODE_7D = 0x7D;

	public static final int TAG_INNDEX = 1;
	public static final int TAG_LEN = 1;
	public static final byte LENGTH_1_2_MASK = (byte) 0b11100000;
	public static final int SEQUENCE_NUM_LENGTH = 2;
	public static final int UTC_TIMESTAMP_LENGTH = 4;
	public static final int UTC_TIMESTAMP_YEAR_OFFSET = 2000;
	public static final byte ACK_MASK = 0b011;
	public static final int TAG_SHIFT = 4;
	public static final int DIAGNOSTICS_LIGHT_LEN = 1;
	public static final int DIAGNOSTICS_CODES_LEN_LEN = 1;
	public static final int DEVICE_ID_LEN = 8;

	public static final String UNDEFINED = "Undefined";

	public static final String[] DEVICE_TYPES = new String[] { "GSM", "CDMA", null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null, null, UNDEFINED };

	public static final int CDMA = 1;
	public static final int DEVICE_ID_INDEX = 2;

}
