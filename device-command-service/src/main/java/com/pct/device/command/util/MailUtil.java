package com.pct.device.command.util;

import javax.mail.BodyPart;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.pct.device.command.entity.DeviceCommand;

@Service
public class MailUtil {
	private static final Logger logger = LoggerFactory.getLogger(MailUtil.class);

	@Autowired(required = true)
	private JavaMailSender sender;

	public String sendMail(DeviceCommand deviceCommand) {
		MimeMessage message = sender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		try {
			String subject = "AT Command Response received for " + deviceCommand.getDeviceId();
			String body = "The Maintenance Server has received a response for the AT Command you sent to " + deviceCommand.getDeviceId() + " at "+ deviceCommand.getSentTimestamp();
			logger.info("Prepare mail object");
			StringBuilder mailBody = new StringBuilder();
			mailBody.append(System.lineSeparator());
			mailBody.append(body);
			mailBody.append(System.lineSeparator());
			mailBody.append(System.lineSeparator());
			if(deviceCommand.getAtCommand() != null) {
				mailBody.append("Command Sent: ");
				mailBody.append(deviceCommand.getAtCommand());
			}
			mailBody.append(System.lineSeparator());
			mailBody.append(System.lineSeparator());
			if(deviceCommand.getDeviceResponse() != null) {
				mailBody.append("Response Received: ");
				mailBody.append(deviceCommand.getDeviceResponse());
			}
			mailBody.append(System.lineSeparator());
			mailBody.append(System.lineSeparator());
			mailBody.append("PCT Support");
			message.setFrom("donotreply@phillips-connect.com");
			helper.setTo(deviceCommand.getCreatedBy());
			helper.setSubject(subject);

			BodyPart messageBodyPart1 = new MimeBodyPart();
			messageBodyPart1.setText(new String(mailBody));
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart1);
			message.setContent(multipart);
			sender.send(message);
			logger.info("Mail send Success!");
		} catch (Exception e) {
			logger.error("Error", e);
			return "Error while sending mail ..";
		}
		return "Mail Sent Success!";
	}
	
	public String responseNotReceivedMail(DeviceCommand deviceCommand) {
		MimeMessage message = sender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		try {
			String subject = "AT Command Response not received for " + deviceCommand.getDeviceId();
			String body = "The Maintenance Server has not yet received a response for the AT Command you sent to " + deviceCommand.getDeviceId() + " at "+ deviceCommand.getSentTimestamp();
			logger.info("Prepare mail object");
			StringBuilder mailBody = new StringBuilder();
			mailBody.append(System.lineSeparator());
			mailBody.append(body);
			mailBody.append(System.lineSeparator());
			mailBody.append(System.lineSeparator());
			if(deviceCommand.getAtCommand() != null) {
				mailBody.append("Command Sent: ");
				mailBody.append(deviceCommand.getAtCommand());
			}
			
			mailBody.append(System.lineSeparator());
			mailBody.append(System.lineSeparator());
			mailBody.append("PCT Support");
			message.setFrom("donotreply@phillips-connect.com");
			helper.setTo(deviceCommand.getCreatedBy());
			helper.setSubject(subject);

			BodyPart messageBodyPart1 = new MimeBodyPart();
			messageBodyPart1.setText(new String(mailBody));
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart1);
			message.setContent(multipart);
			sender.send(message);
			logger.info("Mail send Success!");
		} catch (Exception e) {
			logger.error("Error", e);
			return "Error while sending mail ..";
		}
		return "Mail Sent Success!";
	}
}