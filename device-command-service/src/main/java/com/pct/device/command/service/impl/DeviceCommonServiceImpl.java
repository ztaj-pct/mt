package com.pct.device.command.service.impl;

import java.io.StringWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pct.common.dto.DeviceCommandResponseDTO;
import com.pct.common.model.User;
import com.pct.common.payload.DeviceATCommandReqResponse;
import com.pct.common.util.JwtUser;
import com.pct.device.command.dto.DeviceReportBean;
import com.pct.device.command.dto.MessageDTO;
import com.pct.device.command.dto.ResponseBodyDTO;
import com.pct.device.command.entity.DeviceCommand;
import com.pct.device.command.entity.DeviceCommandResponse;
import com.pct.device.command.entity.Devicereport;
import com.pct.device.command.exception.DeviceCommandException;
import com.pct.device.command.exception.DuplicateCommandException;
import com.pct.device.command.payload.DeviceCommandBean;
import com.pct.device.command.payload.DeviceCommandQueue;
import com.pct.device.command.payload.DeviceCommandRequest;
import com.pct.device.command.payload.DeviceResponse;
import com.pct.device.command.payload.GatewayRequestPayload;
import com.pct.device.command.payload.RedisDeviceCommand;
import com.pct.device.command.repository.DevicereportRepository;
import com.pct.device.command.repository.IDeviceCommandRepository;
import com.pct.device.command.repository.IDeviceCommandResponseRepository;
import com.pct.device.command.repository.RedisDeviceCommandRepository;
import com.pct.device.command.service.IDeviceReportService;
import com.pct.device.command.util.CommandBeanConverter;
import com.pct.device.command.util.Constants;
import com.pct.device.command.util.Logutils;
import com.pct.device.command.util.ValidationUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author sarve
 *
 */
@Slf4j
@Service
public class DeviceCommonServiceImpl  implements IDeviceReportService {

	@Value("${maintreceiver.udp.ip}")
	private String maintListenerIp;
	@Value("${maintreceiver.udp.port}")
	private int maintListenerPort;

	// @Value("${maintenance.kafka.queue}")
	// private String maintenanceKafkaQueue;

	@Value("${atc.maint.reciever.kafka}")
	private String atcMaintenanceKafkaQueue;

	@Value("${atc.reciever.kafka}")
	private String atcRecieverKafkaQueue;

	@Value("${receiver.udp.ip}")
	private String receiverIp;
	@Value("${receiver.udp.port}")
	private int receiverPort;

	@Autowired
	private DatagramSocket socket;

	@Autowired
	private IDeviceCommandRepository deviceCommandRepository;

	@Autowired
	private RedisDeviceCommandRepository redisDeviceCommandRepository;

	@Autowired
	CommandBeanConverter beanConverter;

	@Autowired
	private IDeviceCommandResponseRepository deviceCommandResponseRepository;

	@Autowired
	private DevicereportRepository devicereportRepository;

	private static final List<String> AT_COMMAND_FIELDS = new ArrayList() {
		{
			add("at_command");
			add("priority");
			add("uuid");
		}
	};

	private static final List<String> DEVICE_INFO_FIELDS = new ArrayList() {
		{
			add("deviceId");
			add("deviceIP");
			add("devicePort");
			add("deviceMaintIP");
			add("deviceMaintPort");

		}
	};

	private static final List<String> COMMANDS = new ArrayList() {
		{
			add("command");
			add("uuid");
			add("lastRequestEpoch");
			add("lastCommandSent");
			add("hasPendingCommand");
			add("lastCommandExecuted");
			add("lastResponseEpoch");

		}
	};

	@Transactional
	public DeviceCommand saveDeviceCommand(DeviceCommandRequest deviceCommandRequest, String messageUUID,
			User createdBy, Instant createdAt, String formattedSentTime, boolean isMobile) throws Exception {
		DeviceCommand deviceCommand = new DeviceCommand();
		log.info("saveDeviceCommand  Device Command Reuest recieved on AT-command Processor MessageUUID : "
				+ messageUUID + " Device Id : " + deviceCommandRequest.getDeviceId());
		try {
			if (deviceCommandRequest.getAtCommand() != null
					&& (!deviceCommandRequest.getAtCommand().equalsIgnoreCase(""))) {
				deviceCommandRequest.setAtCommand(deviceCommandRequest.getAtCommand().toLowerCase());
				deviceCommand.setAtCommand(deviceCommandRequest.getAtCommand().trim());
				deviceCommand.setDeviceId(deviceCommandRequest.getDeviceId());
				deviceCommand.setPriority(deviceCommandRequest.getPriority());
				deviceCommand.setSource(deviceCommandRequest.getSource());
				deviceCommand.set_success(deviceCommandRequest.isSuccess());
				deviceCommand.setCreatedDate(createdAt);
				deviceCommand.setStatus("PENDING");
				deviceCommand.setCreatedBy(createdBy.getUserName());
				deviceCommand.setSentTimestamp(Instant.now());
				String Uuid = "";
				boolean isUuidUnique = false;
				Uuid = messageUUID;
				/*
				 * while (!isUuidUnique) { Uuid = UUID.randomUUID().toString(); DeviceCommand
				 * byUuid = deviceCommandRepository.findByUuid(Uuid); if (byUuid == null) {
				 * isUuidUnique = true; } }
				 */
				deviceCommand.setUuid(Uuid);

				List valuesForDevice = redisDeviceCommandRepository
						.findValuesForDevice(Constants.DEVICE_AT_COMMAND + deviceCommand.getDeviceId(), COMMANDS);
				// log.info("after " + valuesForDevice);
				// if the device command list exists for device in redis append the command
				if (valuesForDevice != null && !valuesForDevice.isEmpty() && !(valuesForDevice.get(0) == null)) {

					ObjectMapper objectMapper = new ObjectMapper();
					DeviceCommandQueue deviceCommandQueue = objectMapper.readValue((String) valuesForDevice.get(0),
							DeviceCommandQueue.class);

					boolean deviceCommandAvailable = false;
					// if the device at command already queued do not add again
					for (RedisDeviceCommand redisDeviceCommand : deviceCommandQueue.getRedisDeviceCommand()) {
						if (redisDeviceCommand.getCommand().equals(deviceCommandRequest.getAtCommand().trim())) {
							deviceCommandAvailable = true;
							log.info("Device Command already added to redis MessageUUID : " + messageUUID
									+ " Device Id : " + deviceCommandRequest.getDeviceId());
							if (isMobile) {
								DeviceCommand deviceCommandObj = deviceCommandRepository
										.findGatewayCommandByDeviceIdAndUuid(redisDeviceCommand.getUuid());
								GatewayRequestPayload gatewayRequestPayload = new GatewayRequestPayload();
								gatewayRequestPayload.setUuid(deviceCommandObj.getUuid());
								gatewayRequestPayload.setGatewayId(deviceCommandObj.getDeviceId());
								deleteAtCommand(gatewayRequestPayload);
							} else {
								throw new DuplicateCommandException("AT Command is already queued");
							}
						}
					}
					// add the command to redis command queue
					Map<String, String> redisCommandMap = new HashMap<>();
					RedisDeviceCommand redisDeviceCommand = createCommandObject(deviceCommandRequest, Uuid,
							createdBy.getUserName());

					deviceCommand = deviceCommandRepository.save(deviceCommand);
					log.info("Device Command Saved in DB Id:-" + deviceCommand.getId());
					redisDeviceCommand.setCommandId(formattedSentTime);

					deviceCommandQueue.getRedisDeviceCommand().add(redisDeviceCommand);

					if (deviceCommand.getPriority() != null)
						redisDeviceCommand.setPriority(Integer.parseInt(deviceCommand.getPriority()));
					else
						redisDeviceCommand.setPriority(1);

					redisDeviceCommand.setCreatedEpoch(createdAt.toEpochMilli() + "");
					redisDeviceCommand.setSource(deviceCommandRequest.getSource());

					redisCommandMap.put("command", deviceCommandQueue.toJson());

					// update redis with new set of commands
					log.info("Before sending At Command");
					redisDeviceCommandRepository.add(Constants.DEVICE_AT_COMMAND + deviceCommand.getDeviceId(),
							redisCommandMap);
					log.info("Before deviceCommand repository call for saving device command object MessageUUID : "
							+ messageUUID + " Device Id : " + deviceCommandRequest.getDeviceId());
					log.info("Created By" + deviceCommand.getCreatedBy());
					// save the ATC to database for processing
//					deviceCommand = deviceCommandRepository.save(deviceCommand);

					log.info("Data Stored to redis for MessageUUID : " + messageUUID + " Device Id : "
							+ deviceCommandRequest.getDeviceId());

				} else { // add the command list in redis for the first time
					DeviceCommandQueue redisDeviceCommandList = new DeviceCommandQueue();
					deviceCommand = deviceCommandRepository.save(deviceCommand);
					log.info("Device Command Saved in DB Id:-" + deviceCommand.getId());
					List<RedisDeviceCommand> redisDeviceList = new ArrayList<RedisDeviceCommand>();
					{
						RedisDeviceCommand redisDeviceCommand = new RedisDeviceCommand();
						redisDeviceCommand.setCommand(deviceCommand.getAtCommand());

						if (deviceCommand.getPriority() != null)
							redisDeviceCommand.setPriority(Integer.parseInt(deviceCommand.getPriority()));
						else
							redisDeviceCommand.setPriority(1);

						redisDeviceCommand.setUuid(deviceCommand.getUuid());
						redisDeviceCommand.setCreatedEpoch(createdAt.toEpochMilli() + "");
						redisDeviceCommand.setSource(deviceCommandRequest.getSource());
						redisDeviceCommand.setCreatedBy(createdBy.getUserName());
						redisDeviceCommand.setCommandId(formattedSentTime);
						redisDeviceList.add(redisDeviceCommand);
					}
					redisDeviceCommandList.setRedisDeviceCommand(redisDeviceList);
					Map<String, String> redisCommandMap = new HashMap<>();
					redisCommandMap.put("command", redisDeviceCommandList.toJson());
					log.info("Before sending At Command");
					redisDeviceCommandRepository.add(Constants.DEVICE_AT_COMMAND + deviceCommand.getDeviceId(),
							redisCommandMap);
					log.info("Created By" + deviceCommand.getCreatedBy());
					// deviceCommand = deviceCommandRepository.save(deviceCommand);

					log.info("Data Stored to redis for First Time MessageUUID : " + messageUUID + " Device Id : "
							+ deviceCommandRequest.getDeviceId());
				}

				log.info("Data Stored to redis for device ID   MessageUUID : " + messageUUID + " Device Id : "
						+ deviceCommandRequest.getDeviceId());
			}

			return deviceCommand;
		}

		catch (Exception e) {
			StringWriter sw = Logutils.exception(e);
			log.info(sw.toString());
			log.error("Exception occured while saving AT command " + e.getMessage());
			throw e;
		}

	}

	private String sendNewUDPToListenerThread(String deviceID, String message, Timestamp commandSentTimestamp,
			String deviceIP, int devicePort, String serverIP, int serverPort, String uuid,
			String formattedCommandSentTimestamp) {
		System.out.println("inside");
		// java.sql.Timestamp timestamp = new java.sql.Timestamp(new
		// java.util.Date().getTime());

		InetAddress deviceInetAddress = null;

		try {
			deviceInetAddress = InetAddress.getByName(deviceIP);
		} catch (UnknownHostException ue) {
			ue.printStackTrace();

		}

// packet sent to device comprises:
//////////////////////////////////
// 1) Command text (which is the message parameter to this method
// 2) Identifier - single ASCII of %
// 3) Device ID - ASCII bytes
// 4) Delimiter - single ASCII of ^
// 5) Part of the command up to 40 ASCII characters (bytes)
// 6) Delimiter - single ASCII of ^
// 7) Command Sent Timestamp - 8 bytes of the long containing the nanoseconds

		// byte[] timeBytes = String.format("%0" + Constants.TIMESTAMP_BYTES_LEN + "d",
		// commandSentTimestamp.getTime())
		// .getBytes();
		byte[] timeBytes = formattedCommandSentTimestamp.getBytes();

		byte[] ipBytes = deviceInetAddress.getAddress();

		ByteBuffer portBuffer = ByteBuffer.allocate(Constants.PORT_LEN); // for getTIme which returns long value
		portBuffer.order(ByteOrder.BIG_ENDIAN);
		byte[] portBytes = portBuffer.putInt(devicePort).array();

		byte[] msg = message.getBytes();

		byte[] uuidBytes = uuid.getBytes();

		int sendDataLen = Constants.UDP_RESPONSE_IDENTIFIER_LEN + msg.length + Constants.UDP_RESPONSE_DELIMITER_LEN
				+ deviceID.length() + Constants.UDP_RESPONSE_DELIMITER_LEN + timeBytes.length
				+ Constants.UDP_RESPONSE_DELIMITER_LEN + ipBytes.length + Constants.UDP_RESPONSE_DELIMITER_LEN
				+ portBytes.length + Constants.UDP_RESPONSE_DELIMITER_LEN;

		int commandLen = msg.length;
		int dif = sendDataLen - 63;
		if (dif > 0) {
			commandLen -= dif;
		}

		sendDataLen += commandLen;
		sendDataLen += Constants.UDP_RESPONSE_DELIMITER_LEN + uuidBytes.length;
		byte[] partialCommandBytes = new byte[commandLen];
		System.arraycopy(msg, 0, partialCommandBytes, 0, commandLen);

		String partialCommandSTR = new String(partialCommandBytes);

		byte[] sendData = new byte[sendDataLen];

		int destPosition = 0;

		System.arraycopy(msg, 0, sendData, destPosition, msg.length);
		destPosition += msg.length;

		sendData[destPosition] = Constants.UDP_RESPONSE_IDENTIFIER_BYTE;
		destPosition += 1;

		byte[] id = deviceID.getBytes();
		System.arraycopy(id, 0, sendData, destPosition, deviceID.length());
		destPosition += deviceID.length();

		sendData[destPosition] = Constants.UDP_RESPONSE_DELIMITER_BYTE;
		destPosition += 1;

		System.arraycopy(partialCommandBytes, 0, sendData, destPosition, commandLen);
		destPosition += commandLen;

		sendData[destPosition] = Constants.UDP_RESPONSE_DELIMITER_BYTE;
		destPosition += 1;

		System.arraycopy(timeBytes, 0, sendData, destPosition, Constants.TIMESTAMP_BYTES_LEN);
		destPosition += timeBytes.length;

		sendData[destPosition] = Constants.UDP_COMMAND_IP_DELIMITER_BYTE;
		destPosition += 1;

		System.arraycopy(ipBytes, 0, sendData, destPosition, ipBytes.length);
		destPosition += ipBytes.length;

		sendData[destPosition] = Constants.UDP_RESPONSE_DELIMITER_BYTE;
		destPosition += 1;

		System.arraycopy(portBytes, 0, sendData, destPosition, portBytes.length);
		destPosition += portBytes.length;

		sendData[destPosition] = Constants.UDP_RESPONSE_IDENTIFIER_BYTE;
		destPosition += 1;

		sendData[destPosition] = Constants.UDP_RESPONSE_END_BYTE;
		destPosition += 1;

		System.arraycopy(uuidBytes, 0, sendData, destPosition, uuidBytes.length);
		destPosition += portBytes.length;

		// now send the packet to the device server that will forward it to the device
		//////////////////////////////////////////////////////////////////////////////
		InetAddress deviceServerInetAddress = null;
		try {
			deviceServerInetAddress = InetAddress.getByName(serverIP);
		} catch (UnknownHostException ue) {
			ue.printStackTrace();

		}
		log.info("sending command to maintenance server : " + serverIP + " port :" + serverPort);
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, deviceServerInetAddress, serverPort);

		try {
			// sending command to device in UDP packet
			//////////////////////////////////////////
			log.info("sending command to maintenance server : " + serverIP + " port :" + serverPort);
			socket.send(sendPacket);
			log.info("sent command to maintenance server : " + serverIP + " port :" + serverPort);

		} catch (Exception ie) {
			ie.printStackTrace();

		}

		return partialCommandSTR;
	}

	/*
	 * This method creates the AT Command and send to the maintenance kafka queue
	 * for execution
	 */
	public String sendATCommand(String deviceID, String message, String deviceUuid, boolean isAdhocCommand,
			String formattedCommandSentTimestamp) {
		String partialCommandString = null;
		java.sql.Timestamp commandSentTimestamp = new java.sql.Timestamp(new java.util.Date().getTime());

		try {
			log.info("Inside sendATCommand method for device ID" + deviceID);

			List<String> valuesForDevice = redisDeviceCommandRepository.findValuesForDevice(
					Constants.DEVICE_CURRENT_VIEW_PREFIX + new String(deviceID), DEVICE_INFO_FIELDS);
			String deviceIP = null;
			int devicePort = 0;
			if (valuesForDevice != null && !valuesForDevice.isEmpty() && !(valuesForDevice.get(0) == null)) {
				log.info("Device Ip and Device port found for   Device Id : ");

				if (!(valuesForDevice.get(1) == null && valuesForDevice.get(2) == null)) {
					log.info("Device Ip and Device port found for  " + deviceID + " Device Ip : "
							+ valuesForDevice.get(1) + "Device port " + valuesForDevice.get(2));
					log.info("Maintenance IP and Port Found for the device");
					deviceIP = valuesForDevice.get(1);
					devicePort = Integer.parseInt(valuesForDevice.get(2));
				}

			} else {
				deviceIP = "127.0.0.1";
				devicePort = 90;
				log.info("Device Ip and Device port not found for   Device Id : ");
			}

			partialCommandString = sendNewUDPToListenerThread(deviceID, message, commandSentTimestamp, deviceIP,
					devicePort, maintListenerIp, maintListenerPort, deviceUuid, formattedCommandSentTimestamp);

		} catch (Exception ie) {
			StringWriter sw = Logutils.exception(ie);
			log.info(sw.toString());
			log.error("Exception occured while sending AT-command packet to  SendNewUDPToMaintenance Queue:  "
					+ ie.getMessage());
			return Constants.SENDING_FAILED + " " + ie.getMessage();
		}
		return partialCommandString;

	}

	public DeviceATCommandReqResponse sendOnDemandCommand(DeviceCommandRequest deviceCommandRequest, String messageUUID,
			User createdBy, String formattedCommandSentTimestamp, java.sql.Timestamp commandSentTimestamp,
			boolean isMobile) throws Exception {

		log.info("Inside sendOnDemandCommand method in DeviceCommand Service class with deviceCommand request");

		// commandSentTimestamp.toInstant()
		Instant createdAt = commandSentTimestamp.toInstant();
		// Find Device in redis. If device has reported then data should be available in
		// redis.
		// This is required to find the IP and port for the device.
		List<String> valuesForDevice = redisDeviceCommandRepository.findValuesForDevice(
				Constants.DEVICE_CURRENT_VIEW_PREFIX + deviceCommandRequest.getDeviceId(), DEVICE_INFO_FIELDS);
		log.info("sendOnDemandCommand Redis Values " + valuesForDevice);
		// if (valuesForDevice != null && !valuesForDevice.isEmpty() &&
		// !(valuesForDevice.get(0) == null
		// && valuesForDevice.get(1) == null && valuesForDevice.get(2) == null)) {
		// log.info("IP Address for Device from redis " + valuesForDevice.get(1) + "
		// Port for Device from Redis "
		// + valuesForDevice.get(2));
		if (deviceCommandRequest.getSource() == null) {
			deviceCommandRequest.setSource("API");
		}
		DeviceCommand deviceCommand = saveDeviceCommand(deviceCommandRequest, messageUUID, createdBy, createdAt,
				formattedCommandSentTimestamp, isMobile);
		DeviceATCommandReqResponse deviceAtCommand = null;
		if (deviceCommand != null) {
			deviceAtCommand = beanConverter.deviceCommanBeanToDtoConverter(deviceCommand);
		}
		/*
		 * DeviceCommandReponse deviceRep = new DeviceCommandReponse();
		 * deviceRep.setAtCommand(deviceCommand.getAtCommand());
		 * deviceRep.setDeviceId(deviceCommand.getDeviceId());
		 * deviceRep.setUuid(deviceCommand.getUuid());
		 */
		return deviceAtCommand;
		// } else {
		// IF device is not available that means the device has not reported.
		// TODO need to handle this.
		// throw new Exception("Device Id not found in redis");
		// }
	}

	private RedisDeviceCommand createCommandObject(DeviceCommandRequest deviceCommandRequest, String uuid,
			String createdBy) {
		RedisDeviceCommand redisDeviceCommand = new RedisDeviceCommand();
		if (deviceCommandRequest.getAtCommand() != null) {
			redisDeviceCommand.setCommand(deviceCommandRequest.getAtCommand().trim());
		}
		redisDeviceCommand.setPriority(1);
		redisDeviceCommand.setCreatedBy(createdBy);
		redisDeviceCommand.setUuid(uuid);

		if (deviceCommandRequest.getPriority() != null)
			redisDeviceCommand.setPriority(Integer.parseInt(deviceCommandRequest.getPriority()));
		else
			redisDeviceCommand.setPriority(1);
		redisDeviceCommand.setCreatedEpoch(System.currentTimeMillis() + "");
		return redisDeviceCommand;

	}

	public String atCommandResponseProcessor(DeviceResponse deviceReponse) {
		log.info("inside atCommandResponseProcessor method ");
		try {
			List<DeviceCommand> deviceList = deviceCommandRepository
					.findByDeviceIdAndAtCommand(deviceReponse.getDeviceID(), deviceReponse.getCommandSent());
			if (deviceList != null && deviceList.size() > 0) {
				log.info("found entry in database for command" + deviceReponse.getCommandSent());
				DeviceCommand deviceCommand = deviceList.get(0);
				deviceCommand.set_success(true);
				deviceCommand.setUpdatedDate(Instant.now());
				deviceCommand.setDeviceResponse(deviceReponse.getResponseReceived());
				deviceCommand.setRawReport(deviceReponse.getRawResponse());
				deviceCommand.setResponseServerIp(receiverIp);
				deviceCommand.setResponseServerPort(receiverPort);
				deviceCommand.setStatus("COMPLETED");
				deviceCommandRepository.save(deviceCommand);

				List valuesForDevice = redisDeviceCommandRepository
						.findValuesForDevice(Constants.DEVICE_AT_COMMAND + deviceReponse.getDeviceID(), COMMANDS);

				if (valuesForDevice != null && !valuesForDevice.isEmpty() && !(valuesForDevice.get(0) == null)) {
					ObjectMapper objectMapper = new ObjectMapper();
					List<RedisDeviceCommand> redisDeviceList = new ArrayList<RedisDeviceCommand>();
					DeviceCommandQueue redisDeviceCommandList1 = new DeviceCommandQueue();
					DeviceCommandQueue redisDeviceCommandList = objectMapper.readValue((String) valuesForDevice.get(0),
							DeviceCommandQueue.class);
					boolean deviceCommandAvailable = false;
					for (RedisDeviceCommand redisDeviceCommand : redisDeviceCommandList.getRedisDeviceCommand()) {
						if (redisDeviceCommand.getCommand().equals(deviceReponse.getCommandSent())) {
							deviceCommandAvailable = true;
							log.info("Found command in redis");
						} else {
							RedisDeviceCommand redisDeviceCommandNew = new RedisDeviceCommand();
							redisDeviceCommandNew.setCommand(redisDeviceCommand.getCommand());
							redisDeviceCommandNew.setPriority(redisDeviceCommand.getPriority());
							redisDeviceCommandNew.setUuid(redisDeviceCommand.getUuid());
							redisDeviceList.add(redisDeviceCommandNew);
							redisDeviceCommandNew.setCreatedEpoch(redisDeviceCommand.getCreatedEpoch());
						}
					}
					redisDeviceCommandList1.setLastCommandSent(redisDeviceCommandList.getLastCommandSent());
					redisDeviceCommandList1.setLastRequestEpoch(redisDeviceCommandList.getLastResponseEpoch());
					redisDeviceCommandList1.setRedisDeviceCommand(redisDeviceList);
					Map<String, String> redisCommandMap = new HashMap<>();
					redisDeviceCommandList1.setLastResponseEpoch(System.currentTimeMillis());
					// redisDeviceCommandList1.setLastCommandSent(commandBytes+"");
					if (redisDeviceCommandList1.getRedisDeviceCommand() != null
							&& redisDeviceCommandList1.getRedisDeviceCommand().size() > 0)
						redisDeviceCommandList1.setHasPendingCommand(true);
					else
						redisDeviceCommandList1.setHasPendingCommand(false);
					redisDeviceCommandList1.setLastCommandExecuted(true);

					redisCommandMap.put("command", redisDeviceCommandList1.toJson());

					redisDeviceCommandRepository.add(Constants.DEVICE_AT_COMMAND + deviceReponse.getDeviceID(),
							redisCommandMap);
					log.info("response updated for at command in database");
				}
			}
			return deviceReponse.getCommandSent();

		} catch (Exception e) {
			StringWriter sw = Logutils.exception(e);
			log.info(sw.toString());
			e.printStackTrace();
			return null;
		}
	}

	public String deleteAtCommand(GatewayRequestPayload deviceCommandRequest) {
		log.info("inside atCommandResponseProcessor method ");
		try {
			List<DeviceCommand> deviceList = deviceCommandRepository
					.findByDeviceIdAndUUID(deviceCommandRequest.getGatewayId(), deviceCommandRequest.getUuid());
			if (deviceList != null && deviceList.size() > 0) {
				log.info("found entry in database for command" + deviceCommandRequest.getUuid());
				DeviceCommand deviceCommand = deviceList.get(0);
				deviceCommand.set_success(false);
				deviceCommand.setUpdatedDate(Instant.now());
				deviceCommand.setStatus("DELETED");
				deviceCommandRepository.save(deviceCommand);

				List valuesForDevice = redisDeviceCommandRepository.findValuesForDevice(
						Constants.DEVICE_AT_COMMAND + deviceCommandRequest.getGatewayId(), COMMANDS);

				if (valuesForDevice != null && !valuesForDevice.isEmpty() && !(valuesForDevice.get(0) == null)) {
					ObjectMapper objectMapper = new ObjectMapper();
					List<RedisDeviceCommand> redisDeviceList = new ArrayList<RedisDeviceCommand>();
					DeviceCommandQueue redisDeviceCommandList1 = new DeviceCommandQueue();
					DeviceCommandQueue redisDeviceCommandList = objectMapper.readValue((String) valuesForDevice.get(0),
							DeviceCommandQueue.class);
					boolean deviceCommandAvailable = false;
					for (RedisDeviceCommand redisDeviceCommand : redisDeviceCommandList.getRedisDeviceCommand()) {
						if (redisDeviceCommand.getUuid().equals(deviceCommandRequest.getUuid())) {
							deviceCommandAvailable = true;
							log.info("Removing redis Command");
						} else {
							RedisDeviceCommand redisDeviceCommandNew = new RedisDeviceCommand();
							redisDeviceCommandNew.setCommand(redisDeviceCommand.getCommand());
							redisDeviceCommandNew.setPriority(redisDeviceCommand.getPriority());
							redisDeviceCommandNew.setUuid(redisDeviceCommand.getUuid());
							redisDeviceList.add(redisDeviceCommandNew);
							redisDeviceCommandNew.setCreatedEpoch(redisDeviceCommand.getCreatedEpoch());
						}
					}
					redisDeviceCommandList1.setLastCommandSent(redisDeviceCommandList.getLastCommandSent());
					redisDeviceCommandList1.setLastRequestEpoch(redisDeviceCommandList.getLastResponseEpoch());
					redisDeviceCommandList1.setRedisDeviceCommand(redisDeviceList);
					Map<String, String> redisCommandMap = new HashMap<>();
					redisDeviceCommandList1.setLastResponseEpoch(System.currentTimeMillis());
					// redisDeviceCommandList1.setLastCommandSent(commandBytes+"");
					if (redisDeviceCommandList1.getRedisDeviceCommand() != null
							&& redisDeviceCommandList1.getRedisDeviceCommand().size() > 0) {
						redisDeviceCommandList1.setRedisDeviceCommand(redisDeviceCommandList.getRedisDeviceCommand());
						redisDeviceCommandList1.setHasPendingCommand(true);
					} else
						redisDeviceCommandList1.setRedisDeviceCommand(redisDeviceList);
					redisDeviceCommandList1.setHasPendingCommand(false);
					redisDeviceCommandList1.setLastCommandExecuted(true);

					redisCommandMap.put("command", redisDeviceCommandList1.toJson());

					redisDeviceCommandRepository.add(Constants.DEVICE_AT_COMMAND + deviceCommandRequest.getGatewayId(),
							redisCommandMap);
					log.info("response updated for at command in database");
				}
			} else {
				log.info("No comand found for device " + deviceCommandRequest.getGatewayId() + " and uuid "
						+ deviceCommandRequest.getUuid());
			}
			return deviceCommandRequest.getGatewayId();

		} catch (Exception e) {
			StringWriter sw = Logutils.exception(e);
			log.info(sw.toString());
			e.printStackTrace();
			return null;
		}
	}

	public String deleteAllAtCommand(GatewayRequestPayload deviceCommandRequest) {
		log.info("inside atCommandResponseProcessor method ");
		try {
			{
				log.info("found entry in database for command" + deviceCommandRequest.getUuid());

				List valuesForDevice = redisDeviceCommandRepository.findValuesForDevice(
						Constants.DEVICE_AT_COMMAND + deviceCommandRequest.getGatewayId(), COMMANDS);

				if (valuesForDevice != null && !valuesForDevice.isEmpty() && !(valuesForDevice.get(0) == null)) {
					ObjectMapper objectMapper = new ObjectMapper();
					List<RedisDeviceCommand> redisDeviceList = new ArrayList<RedisDeviceCommand>();
					DeviceCommandQueue redisDeviceCommandList1 = new DeviceCommandQueue();
					DeviceCommandQueue redisDeviceCommandList = objectMapper.readValue((String) valuesForDevice.get(0),
							DeviceCommandQueue.class);
					boolean deviceCommandAvailable = false;
					for (RedisDeviceCommand redisDeviceCommand : redisDeviceCommandList.getRedisDeviceCommand()) {
						if (redisDeviceCommand.getUuid().equals(deviceCommandRequest.getUuid())) {
							deviceCommandAvailable = true;
							log.info("Removing redis Command");
						} /*
							 * else { RedisDeviceCommand redisDeviceCommandNew = new RedisDeviceCommand();
							 * redisDeviceCommandNew.setCommand(redisDeviceCommand.getCommand());
							 * redisDeviceCommandNew.setPriority(redisDeviceCommand.getPriority());
							 * redisDeviceCommandNew.setUuid(redisDeviceCommand.getUuid());
							 * redisDeviceList.add(redisDeviceCommandNew);
							 * redisDeviceCommandNew.setCreatedEpoch(redisDeviceCommand.getCreatedEpoch());
							 * }
							 */
					}
					redisDeviceCommandList1.setLastCommandSent(redisDeviceCommandList.getLastCommandSent());
					redisDeviceCommandList1.setLastRequestEpoch(redisDeviceCommandList.getLastResponseEpoch());
					redisDeviceCommandList1.setRedisDeviceCommand(redisDeviceList);
					Map<String, String> redisCommandMap = new HashMap<>();
					redisDeviceCommandList1.setLastResponseEpoch(System.currentTimeMillis());
					// redisDeviceCommandList1.setLastCommandSent(commandBytes+"");
					if (redisDeviceCommandList1.getRedisDeviceCommand() != null
							&& redisDeviceCommandList1.getRedisDeviceCommand().size() > 0)
						redisDeviceCommandList1.setHasPendingCommand(true);
					else
						redisDeviceCommandList1.setHasPendingCommand(false);
					redisDeviceCommandList1.setLastCommandExecuted(true);

					redisCommandMap.put("command", redisDeviceCommandList1.toJson());

					redisDeviceCommandRepository.add(Constants.DEVICE_AT_COMMAND + deviceCommandRequest.getGatewayId(),
							redisCommandMap);
					log.info("response updated for at command in database");
				}
			}

			return deviceCommandRequest.getGatewayId();

		} catch (Exception e) {
			StringWriter sw = Logutils.exception(e);
			log.info(sw.toString());
			e.printStackTrace();
			return null;
		}
	}

	public DeviceCommandRequest getDeviceCommandFromRedisObjects(String deviceId) {
		log.info("Inside getDeviceCommandFromRedis method to get data from redis for DeviceID " + deviceId);
		DeviceCommandRequest deviceCommand = null;
		try {
			List valuesForDevice = redisDeviceCommandRepository
					.findValuesForDevice(Constants.DEVICE_AT_COMMAND + deviceId, COMMANDS);
			// log.info("after " + valuesForDevice);
			if (valuesForDevice != null && !valuesForDevice.isEmpty() && !(valuesForDevice.get(0) == null)) {
				ObjectMapper objectMapper = new ObjectMapper();
				DeviceCommandQueue redisDeviceCommandList = objectMapper.readValue((String) valuesForDevice.get(0),
						DeviceCommandQueue.class);
				if (redisDeviceCommandList.isHasPendingCommand()) {
					if (redisDeviceCommandList.getLastRequestEpoch() - System.currentTimeMillis() < 6000)
						log.info("recently sent AT Command will wait for ms" + 6000);
				}
				// if (redisDeviceCommandList.getRedisDeviceCommand() != null)
				if (redisDeviceCommandList.getRedisDeviceCommand() != null
						&& redisDeviceCommandList.getRedisDeviceCommand().size() > 0) {
					List<RedisDeviceCommand> sortedList = redisDeviceCommandList.getRedisDeviceCommand().stream()
							.sorted(Comparator.comparing(RedisDeviceCommand::getPriority)).collect(Collectors.toList());

					deviceCommand = new DeviceCommandRequest();
					deviceCommand.setAtCommand(sortedList.get(0).getCommand());
					deviceCommand.setPriority(sortedList.get(0).getPriority() + "");
					deviceCommand.setUuid(sortedList.get(0).getUuid());
					deviceCommand.setCreatedEpoch(sortedList.get(0).getCreatedEpoch());
				}

			}
			return deviceCommand;
		} catch (Exception e) {
			log.error("Exception found while getting data from redis curresponding to deviceID " + deviceId);
			return deviceCommand;
		}

	}

	public DeviceCommandQueue getDeviceCommandListFromRedisObjects(String deviceId) {
		log.info("Inside getDeviceCommandListFromRedis method to get data from redis for DeviceID " + deviceId);
		DeviceCommandRequest deviceCommand = null;
		DeviceCommandQueue redisDeviceCommandList = null;
		try {
			List valuesForDevice = redisDeviceCommandRepository
					.findValuesForDevice(Constants.DEVICE_AT_COMMAND + deviceId, COMMANDS);
			log.info("after " + valuesForDevice);
			if (valuesForDevice != null && !valuesForDevice.isEmpty() && !(valuesForDevice.get(0) == null)) {
				ObjectMapper objectMapper = new ObjectMapper();
				redisDeviceCommandList = objectMapper.readValue((String) valuesForDevice.get(0),
						DeviceCommandQueue.class);
				List<RedisDeviceCommand> sortedSet = redisDeviceCommandList.getRedisDeviceCommand().stream()
						.sorted((o1, o2) -> (o1.getPriority()).compareTo(o2.getPriority()))
						.collect(Collectors.toList());

				log.info("*****");
				redisDeviceCommandList.getRedisDeviceCommand().stream()
						.sorted((o1, o2) -> (o1.getPriority()).compareTo(o2.getPriority()))
						.forEach(System.out::println);

				redisDeviceCommandList.setRedisDeviceCommand(sortedSet);

			}
			return redisDeviceCommandList;
		} catch (Exception e) {
			StringWriter sw = Logutils.exception(e);
			log.info(sw.toString());
			log.error("Exception found while getting data from redis curresponding to deviceID " + deviceId);
			log.error("Exception found while getting data from redis curresponding to deviceID " + e.getMessage());

			return redisDeviceCommandList;
		}

	}

	public Page<DeviceCommandBean> getDeviceCommandWithPagination(String deviceId, Pageable pageable) {
		List<DeviceCommandBean> deviceCommandBeanList = new ArrayList<>();
		Page<DeviceCommandBean> deviceCommandBeanDetailList = new PageImpl<>(deviceCommandBeanList);
		Page<DeviceCommand> deviceCommandList = null;
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		log.info(
				"before getting response from getDeviceCommandWithPagination method from DeviceCommand service find All method :");
		// Specification<DeviceCommand> spc =
		// DeviceCommandSpecification.getDeviceCommandSpecification(deviceId,filterValues);
		deviceCommandList = deviceCommandRepository.findGatewayCommandByDeviceId(deviceId, pageable);
		stopWatch.stop();
		log.info(
				"after getting response from getDeviceCommandWithPagination method from DeviceCommand service find All method :");
		StopWatch stopWatchBean = new StopWatch();
		stopWatchBean.start();
		log.info(
				"before getting response from getDeviceCommandWithPagination method from DeviceCommand service find All bean converter method :");
		if (deviceCommandList != null && deviceCommandList.getContent() != null
				&& deviceCommandList.getContent().size() > 0) {
			for (DeviceCommand device : deviceCommandList.getContent()) {
				deviceCommandBeanList.add(beanConverter.convertDeviceCommandToDeviceCommandNew(device));
			}
		}
		if (deviceCommandBeanList != null && deviceCommandBeanList.size() > 0) {
			deviceCommandBeanDetailList = new PageImpl<>(deviceCommandBeanList);
		}
		stopWatchBean.stop();
		log.info(
				"after getting response from getDeviceCommandWithPagination method from DeviceCommand service find All bean converter method :");
		return deviceCommandBeanDetailList;
	}

	public Page<DeviceCommandBean> getCombineDeviceCommandWithPagination(String deviceId, Pageable pageable) {
		List<DeviceCommandBean> deviceCommandBeanList = new ArrayList<>();
		Page<DeviceCommandBean> deviceCommandBeanDetailList = new PageImpl<>(deviceCommandBeanList);
		Page<DeviceCommandResponse> deviceCommandList = null;
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		log.info(
				"before getting response from getDeviceCommandWithPagination method from DeviceCommand service find All method :");
		// Specification<DeviceCommand> spc =
		// DeviceCommandSpecification.getDeviceCommandSpecification(deviceId,filterValues);
		deviceCommandList = deviceCommandResponseRepository.findGatewayCommandByDeviceId(deviceId, pageable);
		stopWatch.stop();
		log.info(
				"after getting response from getDeviceCommandWithPagination method from DeviceCommand service find All method :");
		StopWatch stopWatchBean = new StopWatch();
		stopWatchBean.start();
		log.info(
				"before getting response from getDeviceCommandWithPagination method from DeviceCommand service find All bean converter method :");
		if (deviceCommandList != null && deviceCommandList.getContent() != null
				&& deviceCommandList.getContent().size() > 0) {
			for (DeviceCommandResponse device : deviceCommandList.getContent()) {
				deviceCommandBeanList.add(beanConverter.convertDeviceCommandResponseToDeviceCommand(device));
			}
		}
		if (deviceCommandBeanList != null && deviceCommandBeanList.size() > 0) {
			deviceCommandBeanDetailList = new PageImpl<>(deviceCommandBeanList);
		}
		stopWatchBean.stop();
		log.info(
				"after getting response from getDeviceCommandWithPagination method from DeviceCommand service find All bean converter method :");
		return deviceCommandBeanDetailList;
	}

	@Override
	public DeviceATCommandReqResponse getATCommandResponse(String uuid) {
		return beanConverter
				.deviceCommanBeanToDtoConverter(deviceCommandRepository.findGatewayCommandByDeviceIdAndUuid(uuid));
	}

	@Override
	public DeviceATCommandReqResponse getATCommandLatestResponse(String deviceId) {
		return beanConverter.deviceCommanBeanToDtoConverter(
				deviceCommandRepository.findGatewayCommandByDeviceIdAndLatestTimeStampt("AT+XINTPMS", deviceId));
	}

	@Override
	public Page<DeviceReportBean> getRawReportWithPagination(String deviceId, Pageable pageable) {
		// TODO Auto-generated method stub
		return null;
	}

	public DeviceATCommandReqResponse saveDeviceCommandForDev(DeviceCommandRequest deviceCommandRequest,
			String messageUUID, User createdBy) throws Exception {
		DeviceCommand deviceCommand = new DeviceCommand();
		log.info("saveDeviceCommand  Device Command Reuest recieved on AT-command Processor MessageUUID : "
				+ messageUUID + " Device Id : " + deviceCommandRequest.getDeviceId());
		java.sql.Timestamp commandSentTimestamp = new java.sql.Timestamp(new java.util.Date().getTime());
		try {
			deviceCommandRequest.setAtCommand(deviceCommandRequest.getAtCommand().toLowerCase());
			deviceCommand.setAtCommand(deviceCommandRequest.getAtCommand().trim());
			deviceCommand.setDeviceId(deviceCommandRequest.getDeviceId());
			deviceCommand.setPriority(deviceCommandRequest.getPriority());
			deviceCommand.setSource("API");
			deviceCommand.set_success(true);
			deviceCommand.setCreatedDate(commandSentTimestamp.toInstant());
			deviceCommand.setStatus("COMPLETED");
			deviceCommand.setCreatedBy(createdBy.getUserName());
			if (deviceCommandRequest.getAtCommand().equalsIgnoreCase("AT+XINABSR")) {
				deviceCommand.setDeviceResponse("C-Status: ONLINE \r\n" + "Vendor:   HEGEMON \r\n"
						+ "PlcVer:   255.64 \r\n" + "AbsVer:   0x12AB \r\n" + "Make:     HALDEX \r\n"
						+ "Model:    ASDFGHJKLqwertyuiopz \r\n" + "SN:       ASDFGHJKLqwertyuiopz \r\n"
						+ "VIN:      ASDFGHJKL01234567 \r\n" + "Voltage:  12345mV \r\n" + "Lamp:     ON \r\n"
						+ "Brake:    OFF \r\n" + "Faults:   1 \r\n" + "OK ");
			} else if (deviceCommandRequest.getAtCommand().equalsIgnoreCase("AT+XINTPMS")) {
				deviceCommand.setDeviceResponse("C-Status: ONLINE \r\n" + "Vendor:   PSI \r\n" + "Sensors:  14 \r\n"
						+ " Loc: Sensor-ID,S-Status, Pressure,Temp,Battery \r\n"
						+ "0x26:0x00461F39,  ONLINE, 3375mBar, 23C,NORMAL \r\n"
						+ "0x24:0x004443ED,  ONLINE, 3375mBar, 23C,NORMAL \r\n"
						+ "0x21:0x00512217,  ONLINE, 3375mBar, 23C,NORMAL \r\n"
						+ "0x23:0x0045A5EC,  ONLINE, 3375mBar, 23C,NORMAL \r\n"
						+ "0x25:0x0058302C,  ONLINE, 3400mBar, 23C,NORMAL \r\n"
						+ "0x22:0x00511FCD,  ONLINE, 3375mBar, 23C,NORMAL \r\n" + "0x27:0x00582F96, OFFLINE \r\n"
						+ "0x28:0x0058320B, OFFLINE \r\n" + "0x41:0x00462188, OFFLINE \r\n"
						+ "0x44:0x0074D194, OFFLINE \r\n" + "0x45:0x0075ADB3, OFFLINE \r\n"
						+ "0x48:0x007CE078, OFFLINE \r\n" + "0x49:0x00590000,  ONLINE, 7900mBar, 26C,NORMAL \r\n"
						+ "0x4a:0x005B0000,  ONLINE, 7900mBar, 26C,NORMAL \r\n" + "OK ");
			} else {
				deviceCommand.setDeviceResponse("OK");
			}
			deviceCommand.setSentTimestamp(Instant.now());
			String Uuid = "";
			Uuid = messageUUID;
			deviceCommand.setUuid(Uuid);
			deviceCommand = deviceCommandRepository.save(deviceCommand);
			DeviceATCommandReqResponse deviceAtCommand = null;
			if (deviceCommand != null) {
				deviceAtCommand = beanConverter.deviceCommanBeanToDtoConverter(deviceCommand);
			}
			return deviceAtCommand;
		}

		catch (Exception e) {
			StringWriter sw = Logutils.exception(e);
			log.info(sw.toString());
			log.error("Exception occured while saving AT command " + e.getMessage());
			throw e;
		}
	}
	
	public ResponseBodyDTO sendAtcRequestForMobile(Map<String, Object> sensorDetails) {
		UUID uuid = UUID.randomUUID();
		String messageUUID = uuid.toString();
		if (sensorDetails == null) {
			throw new DeviceCommandException("Please send valid data to update");
		}
		String atcommand = (String) sensorDetails.get("at-command");
		String deviceId = (String) sensorDetails.get("deviceId");
		if (atcommand == null) {
			throw new DeviceCommandException("Please enter valid atcommand");
		}
		log.info("Inside sendAtcRequestForMobile Api Controller MessageUUID : " + messageUUID + " Device Id : "
				+ deviceId);
		try {
			log.info("before sendAtcRequestForMobile method call from service");
			DeviceCommandRequest deviceCommandRequest = new DeviceCommandRequest();
			deviceCommandRequest.setAtCommand(atcommand);
			deviceCommandRequest.setDeviceId(deviceId);
			deviceCommandRequest.setPriority("-1");
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			log.info("Username : " + jwtUser.getUsername());
			User user = new User();
			user.setUserName(jwtUser.getUsername());
			log.info("Username After getting user object: " + user.getUserName());

			boolean isDummy = false;
			try {
				if (sensorDetails.containsKey("isDummy")) {
					isDummy = (boolean) sensorDetails.get("isDummy");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			DeviceATCommandReqResponse deviceCommand;
			if (isDummy) {
				deviceCommand = saveDeviceCommandForDev(deviceCommandRequest, messageUUID, user);
			} else {
				java.sql.Timestamp commandSentTimestamp = new java.sql.Timestamp(new java.util.Date().getTime());
				String formattedCommandSentTimestamp = String.format("%0" + Constants.TIMESTAMP_BYTES_LEN + "d",
						commandSentTimestamp.getTime());
				deviceCommand = sendOnDemandCommand(deviceCommandRequest, messageUUID, user,
						formattedCommandSentTimestamp, commandSentTimestamp, true);
				if (deviceCommand != null) {
					sendATCommand(deviceCommandRequest.getDeviceId(), deviceCommandRequest.getAtCommand(), messageUUID,
							true, formattedCommandSentTimestamp);
				}
			}
			Map<String, Object> resp = new HashMap<>();
			resp.put("at_command_uuid", deviceCommand.getUuid());
			log.info("Before Fetching AT Command Details");
			TimeUnit.SECONDS.sleep(3);
			DeviceCommandResponseDTO deviceCommandObj = deviceCommandRepository
					.findGatewayCommandByDeviceUuid(deviceCommand.getUuid());
			log.info("After Fetching AT Command Details status " + deviceCommandObj.getStatus());
			if (!ValidationUtils.isEmpty(deviceCommandObj.getStatus())
					&& !deviceCommandObj.getStatus().equals("COMPLETED")) {
				log.info("Status Not completed retry " + deviceCommandObj.getStatus());
				TimeUnit.SECONDS.sleep(3);
				deviceCommandObj = deviceCommandRepository.findGatewayCommandByDeviceUuid(deviceCommand.getUuid());
			}
			if (!ValidationUtils.isEmpty(deviceCommandObj.getStatus())
					&& !deviceCommandObj.getStatus().equals("COMPLETED")) {
				log.info("Status Not completed retry " + deviceCommandObj.getStatus());
				TimeUnit.SECONDS.sleep(3);
				deviceCommandObj = deviceCommandRepository.findGatewayCommandByDeviceUuid(deviceCommand.getUuid());
				log.info("second time 11 second sleep  " + deviceCommandObj.getUuid() + " "
						+ deviceCommandObj.getStatus());
			}
			if (!ValidationUtils.isEmpty(deviceCommandObj.getStatus())
					&& !deviceCommandObj.getStatus().equals("COMPLETED")) {
				TimeUnit.SECONDS.sleep(3);
				log.info("Status Not completed retry " + deviceCommandObj.getStatus());
				deviceCommandObj = deviceCommandRepository.findGatewayCommandByDeviceUuid(deviceCommand.getUuid());
				log.info("second time 11 second sleep  " + deviceCommandObj.getUuid() + " "
						+ deviceCommandObj.getStatus());
			}
			if (!ValidationUtils.isEmpty(deviceCommandObj.getStatus())
					&& !deviceCommandObj.getStatus().equals("COMPLETED")) {
				TimeUnit.SECONDS.sleep(3);
				deviceCommandObj = deviceCommandRepository.findGatewayCommandByDeviceUuid(deviceCommand.getUuid());
				log.info("second time 11 second sleep  " + deviceCommandObj.getUuid() + " "
						+ deviceCommandObj.getStatus());
			}
			if (!ValidationUtils.isEmpty(deviceCommandObj.getStatus())
					&& deviceCommandObj.getStatus().equals("COMPLETED")) {
				log.info("Status Completed response");
				resp.put("at_command_uuid", deviceCommandObj.getUuid());
				resp.put("device_id", deviceCommandObj.getDeviceId());
				resp.put("at_command", deviceCommandObj.getAtCommand());
				resp.put("device_response", deviceCommandObj.getDeviceResponse());
				resp.put("status", deviceCommandObj.getStatus());
				return new ResponseBodyDTO<>(true, "STATUSCOMPLETED", resp);
			} else if (!ValidationUtils.isEmpty(deviceCommandObj.getStatus())
					&& deviceCommandObj.getStatus().equals("ERROR")) {
				resp.put("at_command_uuid", deviceCommandObj.getUuid());
				resp.put("device_id", deviceCommandObj.getDeviceId());
				resp.put("at_command", deviceCommandObj.getAtCommand());
				resp.put("device_response", deviceCommandObj.getDeviceResponse());
				resp.put("status", deviceCommandObj.getStatus());
				return new ResponseBodyDTO<>(false, "STATUSERROR", resp);
			} else {
				resp.put("at_command_uuid", deviceCommandObj.getUuid());
				resp.put("device_id", deviceCommandObj.getDeviceId());
				resp.put("at_command", deviceCommandObj.getAtCommand());
				resp.put("device_response", deviceCommandObj.getDeviceResponse());
				resp.put("status", deviceCommandObj.getStatus());
				return new ResponseBodyDTO<>(false, "STATUSPENDING", resp);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new DeviceCommandException("Error while sending AT command :- " + e.getMessage());
		}
	}
	
	public ResponseBodyDTO refreshAtcRequestForMobile(Map<String, Object> sensorDetails) {
		if (sensorDetails == null) {
			throw new DeviceCommandException("Please send valid data to refresh");
		}
		String atCommandUuid = (String) sensorDetails.get("at-command-uuid");
		if (atCommandUuid == null) {
			throw new DeviceCommandException("Please enter valid atCommandUuid");
		}

		try {
			log.info("before refreshAtcRequestForMobile method call from service");
			Map<String, Object> resp = new HashMap<>();
			log.info("Before Fetching AT Command Details");
			DeviceCommand deviceCommandObj = deviceCommandRepository.findGatewayCommandByDeviceIdAndUuid(atCommandUuid);
			log.info("After Fetching AT Command Details status " + deviceCommandObj.getStatus());
			if (!ValidationUtils.isEmpty(deviceCommandObj.getStatus())
					&& !deviceCommandObj.getStatus().equals("COMPLETED")) {
				log.info("Status Not completed retry " + deviceCommandObj.getStatus());
				TimeUnit.SECONDS.sleep(3);
				deviceCommandObj = deviceCommandRepository.findGatewayCommandByDeviceIdAndUuid(atCommandUuid);
			}
			if (!ValidationUtils.isEmpty(deviceCommandObj.getStatus())
					&& !deviceCommandObj.getStatus().equals("COMPLETED")) {
				TimeUnit.SECONDS.sleep(3);
				deviceCommandObj = deviceCommandRepository.findGatewayCommandByDeviceIdAndUuid(atCommandUuid);
			}
			if (!ValidationUtils.isEmpty(deviceCommandObj.getStatus())
					&& !deviceCommandObj.getStatus().equals("COMPLETED")) {
				TimeUnit.SECONDS.sleep(3);
				deviceCommandObj = deviceCommandRepository.findGatewayCommandByDeviceIdAndUuid(atCommandUuid);
			}
			if (!ValidationUtils.isEmpty(deviceCommandObj.getStatus())
					&& !deviceCommandObj.getStatus().equals("COMPLETED")) {
				TimeUnit.SECONDS.sleep(3);
				deviceCommandObj = deviceCommandRepository.findGatewayCommandByDeviceIdAndUuid(atCommandUuid);
			}
			if (!ValidationUtils.isEmpty(deviceCommandObj.getStatus())
					&& deviceCommandObj.getStatus().equals("COMPLETED")) {
				log.info("Status Completed response");
				resp.put("at_command_uuid", deviceCommandObj.getUuid());
				resp.put("device_id", deviceCommandObj.getDeviceId());
				resp.put("at_command", deviceCommandObj.getAtCommand());
				resp.put("device_response", deviceCommandObj.getDeviceResponse());
				resp.put("status", deviceCommandObj.getStatus());
				return new ResponseBodyDTO<>(true, "STATUSCOMPLETED", resp);
			} else if (!ValidationUtils.isEmpty(deviceCommandObj.getStatus())
					&& deviceCommandObj.getStatus().equals("ERROR")) {
				resp.put("at_command_uuid", deviceCommandObj.getUuid());
				resp.put("device_id", deviceCommandObj.getDeviceId());
				resp.put("at_command", deviceCommandObj.getAtCommand());
				resp.put("device_response", deviceCommandObj.getDeviceResponse());
				resp.put("status", deviceCommandObj.getStatus());
				return new ResponseBodyDTO<>(false, "STATUSERROR", resp);
			} else {
				resp.put("at_command_uuid", deviceCommandObj.getUuid());
				resp.put("device_id", deviceCommandObj.getDeviceId());
				resp.put("at_command", deviceCommandObj.getAtCommand());
				resp.put("device_response", deviceCommandObj.getDeviceResponse());
				resp.put("status", deviceCommandObj.getStatus());
				return new ResponseBodyDTO<>(false, "STATUSPENDING", resp);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new DeviceCommandException("Error while refreshing AT command :- " + e.getMessage());
		}
	}

	public String deviceCargoCommand(String deviceId, String messageUUID) throws Exception {
		String status = null;
		String commandText = "XCSRN";
		log.info("MessageUUID : " + messageUUID + " started sending cargo command " + commandText + " for device "
				+ deviceId);
		commandText = Constants.AT_COMMAND_PREFIX + commandText.trim();
		commandText = commandText.replaceAll("%2B", "+");
		java.sql.Timestamp commandSentTimestamp = new java.sql.Timestamp(new java.util.Date().getTime());
		String formattedCommandSentTimestamp = String.format("%0" + Constants.TIMESTAMP_BYTES_LEN + "d",
				commandSentTimestamp.getTime());
		DeviceCommandRequest deviceCommandRequest = new DeviceCommandRequest();
		deviceCommandRequest.setAtCommand(commandText);
		deviceCommandRequest.setDeviceId(deviceId);
		deviceCommandRequest.setPriority("-1");
		JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		log.info("Username : " + jwtUser.getUsername());
		User user = new User();
		user.setUserName(jwtUser.getUsername());
		LocalDateTime ldt = LocalDateTime.now().minusDays(10);
		Timestamp earlierTimeStamp = Timestamp.valueOf(ldt);
		log.info("MessageUUID : " + messageUUID + " started getting device report reported in the last 10 days "
				+ commandText + " for device " + deviceId);
		Optional<Devicereport> findDeviceReport = devicereportRepository.findDeviceReport(deviceId, earlierTimeStamp);
		log.info("MessageUUID : " + messageUUID + " comptleted getting device report reported in the last 10 days "
				+ commandText + " for device " + deviceId);
		if (!findDeviceReport.isPresent()) {
			status = "ERROR - Device hasn't reported in the last 10 days so device IP/Port can not be obtained!";
			log.info("MessageUUID : " + messageUUID + " " + status + commandText + " for device " + deviceId);
			return status;
		}
		DeviceATCommandReqResponse deviceCommand = sendOnDemandCommand(deviceCommandRequest, messageUUID, user,
				formattedCommandSentTimestamp, commandSentTimestamp, true);
		if (deviceCommand != null) {
			String atCommand = sendATCommand(deviceId, commandText, messageUUID, true, formattedCommandSentTimestamp);
			log.info("MessageUUID : " + messageUUID + " Before Fetching AT Command Details for uuid " + deviceCommand.getUuid());
			TimeUnit.SECONDS.sleep(3);
			DeviceCommandResponseDTO deviceCommandObj = deviceCommandRepository
					.findGatewayCommandByDeviceUuid(deviceCommand.getUuid());
			log.info("MessageUUID : " + messageUUID + " After Fetching AT Command Details status " + deviceCommand.getUuid());
			if (!ValidationUtils.isEmpty(deviceCommandObj.getStatus())
					&& deviceCommandObj.getStatus().equals("COMPLETED")) {
				status = "SUCCESS - device responded to " + commandText;
				log.info("MessageUUID : " + messageUUID + " " + status);
				return status;
			}
			status = "INFO - " + atCommand + " command has been SENT to device";
			log.info("MessageUUID : " + messageUUID + " " + status + " for device " + deviceId);
			return status;
		} else {
			log.info("MessageUUID : " + messageUUID + " deviceCommand null while updating redis ");
			return null;
		}
	}


	public com.pct.device.command.payload.DeviceCommandResponse checkDeviceCommandExists(DeviceCommandRequest deviceCommandRequest, String messageUUID)
			throws Exception {
		log.info("MessageUUID : " + messageUUID + " checking at-command " + deviceCommandRequest.getAtCommand()
				+ " for device " + deviceCommandRequest.getDeviceId() + " in redis ");
		List<String> valuesForDevice = redisDeviceCommandRepository
				.findValuesForDevice(Constants.DEVICE_AT_COMMAND + deviceCommandRequest.getDeviceId(), COMMANDS);
		com.pct.device.command.payload.DeviceCommandResponse deviceCommandResponse = new com.pct.device.command.payload.DeviceCommandResponse();
		if (valuesForDevice != null && !valuesForDevice.isEmpty() && !(valuesForDevice.get(0) == null)) {
			log.info("MessageUUID : " + messageUUID + " device " + deviceCommandRequest.getDeviceId() + " size is "
					+ valuesForDevice.size());
			ObjectMapper objectMapper = new ObjectMapper();
			DeviceCommandQueue deviceCommandQueue = objectMapper.readValue(valuesForDevice.get(0),
					DeviceCommandQueue.class);
			for (RedisDeviceCommand redisDeviceCommand : deviceCommandQueue.getRedisDeviceCommand()) {
				if (redisDeviceCommand.getCommand().equals(deviceCommandRequest.getAtCommand().trim())) {
					log.info("Device Command already added to redis MessageUUID : " + messageUUID + " Device Id : "
							+ deviceCommandRequest.getDeviceId());
					deviceCommandResponse.setDeviceCommand(
							"AT command is already in queued for device " + deviceCommandRequest.getDeviceId());
					return deviceCommandResponse;
				}
			}
			log.info("MessageUUID : " + messageUUID + " AT command not exists for device  "
					+ deviceCommandRequest.getDeviceId());
			deviceCommandResponse
					.setDeviceCommand("AT command not exists for device " + deviceCommandRequest.getDeviceId());
			return deviceCommandResponse;
		}
		log.info("MessageUUID : " + messageUUID + " no device " + deviceCommandRequest.getDeviceId() + " : size is  "
				+ valuesForDevice.size());
		deviceCommandResponse
				.setDeviceCommand("AT command not exists for device " + deviceCommandRequest.getDeviceId());
		return deviceCommandResponse;
	}

	public com.pct.device.command.payload.DeviceCommandResponse atcCache(String deviceId, String messageUUID) throws Exception {
		log.info("MessageUUID : " + messageUUID + " gettin at-commands for device " + deviceId + " in redis ");
		List<String> valuesForDevice = redisDeviceCommandRepository
				.findValuesForDevice(Constants.DEVICE_AT_COMMAND + deviceId, COMMANDS);
		com.pct.device.command.payload.DeviceCommandResponse deviceCommandResponse = new com.pct.device.command.payload.DeviceCommandResponse();
		if (valuesForDevice != null && !valuesForDevice.isEmpty() && !(valuesForDevice.get(0) == null)) {
			ObjectMapper objectMapper = new ObjectMapper();
			DeviceCommandQueue deviceCommandQueue = objectMapper.readValue(valuesForDevice.get(0),
					DeviceCommandQueue.class);
			List<String> atCommands = new ArrayList<>();
			for (RedisDeviceCommand redisDeviceCommand : deviceCommandQueue.getRedisDeviceCommand()) {
				atCommands.add(redisDeviceCommand.getCommand());
			}
			deviceCommandResponse.setDeviceId(deviceId);
			deviceCommandResponse.setStatus("Success");
			;
			deviceCommandResponse.setAtCommands(atCommands);
			return deviceCommandResponse;
		}
		deviceCommandResponse.setDeviceId(deviceId);
		deviceCommandResponse.setStatus("Success");
		deviceCommandResponse.setAtCommands(null);
		return deviceCommandResponse;
	}
	
	public String deviceCameraCommand(String deviceId, String messageUUID) throws Exception {
		String commandText = "XCMRARN";
		String status = null;
		log.info("MessageUUID : " + messageUUID + " started sending camera command " + commandText + " for device "
				+ deviceId);
		commandText = Constants.AT_COMMAND_PREFIX + commandText.trim();
		commandText = commandText.replaceAll("%2B", "+");
		java.sql.Timestamp commandSentTimestamp = new java.sql.Timestamp(new java.util.Date().getTime());
		String formattedCommandSentTimestamp = String.format("%0" + Constants.TIMESTAMP_BYTES_LEN + "d",
				commandSentTimestamp.getTime());
		DeviceCommandRequest deviceCommandRequest = new DeviceCommandRequest();
		deviceCommandRequest.setAtCommand(commandText);
		deviceCommandRequest.setDeviceId(deviceId);
		deviceCommandRequest.setPriority("-1");
		JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		log.info("Username : " + jwtUser.getUsername());
		User user = new User();
		user.setUserName(jwtUser.getUsername());
		log.info("Username After getting user object: " + user.getUserName());
		LocalDateTime ldt = LocalDateTime.now().minusDays(10);
		Timestamp earlierTimeStamp = Timestamp.valueOf(ldt);
		log.info("MessageUUID : " + messageUUID + " started getting device report reported in the last 10 days "
				+ commandText + " for device " + deviceId);
		Optional<Devicereport> findDeviceReport = devicereportRepository.findDeviceReport(deviceId, earlierTimeStamp);
		log.info("MessageUUID : " + messageUUID + " comptleted getting device report reported in the last 10 days "
				+ commandText + " for device " + deviceId);
		if (!findDeviceReport.isPresent()) {
			status = "ERROR - Device hasn't reported in the last 10 days so device IP/Port can not be obtained!";
			log.info("MessageUUID : " + messageUUID + " " + status + " for device " + deviceId);
			return status;
		}
		DeviceATCommandReqResponse deviceCommand = sendOnDemandCommand(deviceCommandRequest, messageUUID, user,
				formattedCommandSentTimestamp, commandSentTimestamp, true);
		if (deviceCommand != null) {
			String atCommand = sendATCommand(deviceId, commandText, messageUUID, true, formattedCommandSentTimestamp);
			log.info("Before Fetching AT Command Details for uuid " + deviceCommand.getUuid());
			TimeUnit.SECONDS.sleep(3);
			DeviceCommandResponseDTO deviceCommandObj = deviceCommandRepository
					.findGatewayCommandByDeviceUuid(deviceCommand.getUuid());
			log.info("After Fetching AT Command Details status " + deviceCommand.getUuid());
			if (!ValidationUtils.isEmpty(deviceCommandObj.getStatus())
					&& deviceCommandObj.getStatus().equals("COMPLETED")) {
				status = "SUCCESS - device responded to " + commandText;
				log.info("MessageUUID : " + messageUUID + " " + status);
				return status;
			}
			status = "INFO - " + atCommand + " command has been SENT to device";
			log.info("MessageUUID : " + messageUUID + "  " + status + " for device " + deviceId);
			return status;
		} else {
			log.info("MessageUUID : " + messageUUID + " deviceCommand null while updating redis ");
			return null;
		}
	}
	
	public String deviceGladhandLockCommand(String deviceId, String messageUUID) throws Exception {
		String status = null;
		String commandText = "XBTGHLO";
		log.info("MessageUUID : " + messageUUID + " started sending lock command " + commandText + " for device "
				+ deviceId);
		commandText = Constants.AT_COMMAND_PREFIX + commandText.trim();
		commandText = commandText.replaceAll("%2B", "+");
		java.sql.Timestamp commandSentTimestamp = new java.sql.Timestamp(new java.util.Date().getTime());
		String formattedCommandSentTimestamp = String.format("%0" + Constants.TIMESTAMP_BYTES_LEN + "d",
				commandSentTimestamp.getTime());
		DeviceCommandRequest deviceCommandRequest = new DeviceCommandRequest();
		deviceCommandRequest.setAtCommand(commandText);
		deviceCommandRequest.setDeviceId(deviceId);
		deviceCommandRequest.setPriority("-1");
		JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		log.info("Username : " + jwtUser.getUsername());
		User user = new User();
		user.setUserName(jwtUser.getUsername());
		log.info("Username After getting user object: " + user.getUserName());
		LocalDateTime ldt = LocalDateTime.now().minusDays(10);
		Timestamp earlierTimeStamp = Timestamp.valueOf(ldt);
		log.info("MessageUUID : " + messageUUID + " started getting device report reported in the last 10 days "
				+ commandText + " for device " + deviceId);
		Optional<Devicereport> findDeviceReport = devicereportRepository.findDeviceReport(deviceId, earlierTimeStamp);
		log.info("MessageUUID : " + messageUUID + " comptleted getting device report reported in the last 10 days "
				+ commandText + " for device " + deviceId);
		if (!findDeviceReport.isPresent()) {
			status = "ERROR - Device hasn't reported in the last 10 days so device IP/Port can not be obtained!";
			log.info("MessageUUID : " + messageUUID + " " + status + " for device " + deviceId);
			return status;
		}
		DeviceATCommandReqResponse deviceCommand = sendOnDemandCommand(deviceCommandRequest, messageUUID, user,
				formattedCommandSentTimestamp, commandSentTimestamp, true);
		if (deviceCommand != null) {
			String atCommand = sendATCommand(deviceId, commandText, messageUUID, true, formattedCommandSentTimestamp);
			log.info("Before Fetching AT Command Details for uuid " + deviceCommand.getUuid());
			TimeUnit.SECONDS.sleep(3);
			DeviceCommandResponseDTO deviceCommandObj = deviceCommandRepository
					.findGatewayCommandByDeviceUuid(deviceCommand.getUuid());
			log.info("After Fetching AT Command Details status " + deviceCommand.getUuid());
			if (!ValidationUtils.isEmpty(deviceCommandObj.getStatus())
					&& deviceCommandObj.getStatus().equals("COMPLETED")) {
				status = "SUCCESS - device responded to " + commandText;
				log.info("MessageUUID : " + messageUUID + " " + status);
				return status;
			}
			status = "INFO - " + atCommand + " command has been SENT to device";
			log.info("MessageUUID : " + messageUUID + " " + status + " for device " + deviceId);
			return status;
		} else {
			log.info("MessageUUID : " + messageUUID + " deviceCommand null while updating redis ");
			return null;
		}
	}

	public String deviceGladhandUnlockCommand(String deviceId, String messageUUID) throws Exception {
		String status = null;
		String commandText = "XBTGHUL";
		log.info("MessageUUID : " + messageUUID + " started sending unlock command " + commandText + " for device "
				+ deviceId);
		commandText = Constants.AT_COMMAND_PREFIX + commandText.trim();
		commandText = commandText.replaceAll("%2B", "+");
		java.sql.Timestamp commandSentTimestamp = new java.sql.Timestamp(new java.util.Date().getTime());
		String formattedCommandSentTimestamp = String.format("%0" + Constants.TIMESTAMP_BYTES_LEN + "d",
				commandSentTimestamp.getTime());
		DeviceCommandRequest deviceCommandRequest = new DeviceCommandRequest();
		deviceCommandRequest.setAtCommand(commandText);
		deviceCommandRequest.setDeviceId(deviceId);
		deviceCommandRequest.setPriority("-1");
		JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		log.info("Username : " + jwtUser.getUsername());
		User user = new User();
		user.setUserName(jwtUser.getUsername());
		log.info("Username After getting user object: " + user.getUserName());
		LocalDateTime ldt = LocalDateTime.now().minusDays(10);
		Timestamp earlierTimeStamp = Timestamp.valueOf(ldt);
		log.info("MessageUUID : " + messageUUID + " started getting device report reported in the last 10 days "
				+ commandText + " for device " + deviceId);
		Optional<Devicereport> findDeviceReport = devicereportRepository.findDeviceReport(deviceId, earlierTimeStamp);
		log.info("MessageUUID : " + messageUUID + " comptleted getting device report reported in the last 10 days "
				+ commandText + " for device " + deviceId);
		if (!findDeviceReport.isPresent()) {
			status = "ERROR - Device hasn't reported in the last 10 days so device IP/Port can not be obtained!";
			log.info("MessageUUID : " + messageUUID + " " + status + " for device " + deviceId);
			return status;
		}
		DeviceATCommandReqResponse deviceCommand = sendOnDemandCommand(deviceCommandRequest, messageUUID, user,
				formattedCommandSentTimestamp, commandSentTimestamp, true);
		if (deviceCommand != null) {
			String atCommand = sendATCommand(deviceId, commandText, messageUUID, true, formattedCommandSentTimestamp);
			log.info("MessageUUID : " + messageUUID + " Before Fetching AT Command Details for uuid " + deviceCommand.getUuid());
			TimeUnit.SECONDS.sleep(3);
			DeviceCommandResponseDTO deviceCommandObj = deviceCommandRepository
					.findGatewayCommandByDeviceUuid(deviceCommand.getUuid());
			log.info("MessageUUID : " + messageUUID + " After Fetching AT Command Details status " + deviceCommand.getUuid());
			if (!ValidationUtils.isEmpty(deviceCommandObj.getStatus())
					&& deviceCommandObj.getStatus().equals("COMPLETED")) {
				status = "SUCCESS - device responded to " + commandText;
				log.info("MessageUUID : " + messageUUID + " " + status);
				return status;
			}
			status = "INFO - " + atCommand + " command has been SENT to device";
			log.info("MessageUUID : " + messageUUID + " " + status + " for device " + deviceId);
			return status;
		} else {
			log.info("MessageUUID : " + messageUUID + " deviceCommand null while updating redis ");
			return null;
		}
	}

	public String devicePreCheckUDPCommand(String deviceId, String messageUUID) throws Exception {
		String commandText = "XPRECHK";
		String status = null;
		log.info("MessageUUID : " + messageUUID + " started prechecking udp command " + commandText + " for device "
				+ deviceId);
		commandText = Constants.AT_COMMAND_PREFIX + commandText.trim();
		commandText = commandText.replaceAll("%2B", "+");
		java.sql.Timestamp commandSentTimestamp = new java.sql.Timestamp(new java.util.Date().getTime());
		String formattedCommandSentTimestamp = String.format("%0" + Constants.TIMESTAMP_BYTES_LEN + "d",
				commandSentTimestamp.getTime());
		DeviceCommandRequest deviceCommandRequest = new DeviceCommandRequest();
		deviceCommandRequest.setAtCommand(commandText);
		deviceCommandRequest.setDeviceId(deviceId);
		deviceCommandRequest.setPriority("-1");
		JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		log.info("Username : " + jwtUser.getUsername());
		User user = new User();
		user.setUserName(jwtUser.getUsername());
		log.info("Username After getting user object: " + user.getUserName());
		LocalDateTime ldt = LocalDateTime.now().minusDays(10);
		Timestamp earlierTimeStamp = Timestamp.valueOf(ldt);
		log.info("MessageUUID : " + messageUUID + " started getting device report reported in the last 10 days "
				+ commandText + " for device " + deviceId);
		Optional<Devicereport> findDeviceReport = devicereportRepository.findDeviceReport(deviceId, earlierTimeStamp);
		log.info("MessageUUID : " + messageUUID + " comptleted getting device report reported in the last 10 days "
				+ commandText + " for device " + deviceId);
		if (!findDeviceReport.isPresent()) {
			status = "ERROR - Device hasn't reported in the last 10 days so device IP/Port can not be obtained!";
			log.info("MessageUUID : " + messageUUID + " " + status + " for device " + deviceId);
			return status;
		}
		DeviceATCommandReqResponse deviceCommand = sendOnDemandCommand(deviceCommandRequest, messageUUID, user,
				formattedCommandSentTimestamp, commandSentTimestamp, true);
		if (deviceCommand != null) {
			String atCommand = sendATCommand(deviceId, commandText, messageUUID, true, formattedCommandSentTimestamp);
			log.info("MessageUUID : " + messageUUID + " Before Fetching AT Command Details for uuid " + deviceCommand.getUuid());
			TimeUnit.SECONDS.sleep(3);
			DeviceCommandResponseDTO deviceCommandObj = deviceCommandRepository
					.findGatewayCommandByDeviceUuid(deviceCommand.getUuid());
			log.info("MessageUUID : " + messageUUID +  " After Fetching AT Command Details status " + deviceCommand.getUuid());
			if (!ValidationUtils.isEmpty(deviceCommandObj.getStatus())
					&& deviceCommandObj.getStatus().equals("COMPLETED")) {
				status = "SUCCESS - device responded to " + commandText;
				log.info("MessageUUID : " + messageUUID + " " + status);
				return status;
			}
			status = "INFO - " + atCommand + " command has been SENT to device";
			log.info("MessageUUID : " + messageUUID + "  " + status + " for device " + deviceId);
			return status;
		} else {
			log.info("MessageUUID : " + messageUUID + " deviceCommand null while updating redis ");
			return null;
		}
	}

     public String deviceReportNowUDPCommand(String deviceId, String messageUUID) throws Exception {
		String commandText = "XRN";
		String status = null;
		log.info("MessageUUID : " + messageUUID + " started reportingnow udp command " + commandText + " for device "
				+ deviceId);
		commandText = Constants.AT_COMMAND_PREFIX + commandText.trim();
		commandText = commandText.replaceAll("%2B", "+");
		java.sql.Timestamp commandSentTimestamp = new java.sql.Timestamp(new java.util.Date().getTime());
		String formattedCommandSentTimestamp = String.format("%0" + Constants.TIMESTAMP_BYTES_LEN + "d",
				commandSentTimestamp.getTime());
		DeviceCommandRequest deviceCommandRequest = new DeviceCommandRequest();
		deviceCommandRequest.setAtCommand(commandText);
		deviceCommandRequest.setDeviceId(deviceId);
		deviceCommandRequest.setPriority("-1");
		JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		log.info("Username : " + jwtUser.getUsername());
		User user = new User();
		user.setUserName(jwtUser.getUsername());
		log.info("Username After getting user object: " + user.getUserName());
		LocalDateTime ldt = LocalDateTime.now().minusDays(10);
		Timestamp earlierTimeStamp = Timestamp.valueOf(ldt);
		log.info("MessageUUID : " + messageUUID + " started getting device report reported in the last 10 days "
				+ commandText + " for device " + deviceId);
		Optional<Devicereport> findDeviceReport = devicereportRepository.findDeviceReport(deviceId, earlierTimeStamp);
		log.info("MessageUUID : " + messageUUID + " comptleted getting device report reported in the last 10 days "
				+ commandText + " for device " + deviceId);
		if (!findDeviceReport.isPresent()) {
			status = "ERROR - Device hasn't reported in the last 10 days so device IP/Port can not be obtained!";
			log.info("MessageUUID : " + messageUUID + " " + status + " for device " + deviceId);
			return status;
		}
		DeviceATCommandReqResponse deviceCommand = sendOnDemandCommand(deviceCommandRequest, messageUUID, user,
				formattedCommandSentTimestamp, commandSentTimestamp, true);
		if (deviceCommand != null) {
			String atCommand = sendATCommand(deviceId, commandText, messageUUID, true, formattedCommandSentTimestamp);
			log.info("MessageUUID : " + messageUUID + " Before Fetching AT Command Details for uuid " + deviceCommand.getUuid());
			TimeUnit.SECONDS.sleep(3);
			DeviceCommandResponseDTO deviceCommandObj = deviceCommandRepository
					.findGatewayCommandByDeviceUuid(deviceCommand.getUuid());
			log.info("MessageUUID : " + messageUUID +  " After Fetching AT Command Details status " + deviceCommand.getUuid());
			if (!ValidationUtils.isEmpty(deviceCommandObj.getStatus())
					&& deviceCommandObj.getStatus().equals("COMPLETED")) {
				status = "SUCCESS - device responded to " + commandText;
				log.info("MessageUUID : " + messageUUID + " " + status);
				return status;
			}
			status = "INFO - " + atCommand + " command has been SENT to device";
			log.info("MessageUUID : " + messageUUID + "  " + status + " for device " + deviceId);
			return status;
		} else {
			log.info("MessageUUID : " + messageUUID + " deviceCommand null while updating redis ");
			return null;
		}
	}
 	
}