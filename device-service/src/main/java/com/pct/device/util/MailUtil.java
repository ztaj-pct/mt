package com.pct.device.util;

import java.util.List;

import javax.mail.BodyPart;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.pct.common.model.User;
import com.pct.device.payload.AssociationPayload;

@Service
public class MailUtil {
	private static final Logger logger = LoggerFactory.getLogger(MailUtil.class);

	@Autowired(required = true)
	private JavaMailSender sender;

	@Value("${export.url}")
	private String url;

	public String sendMail(User user, String filePath, boolean deviceList, String fileName) {
		MimeMessage message = sender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		try {
			String subject = "Requested Data Export is Ready to Download";
			String body = "The data export you requested is ready to download:";
			logger.info("Prepare mail object");
			StringBuilder mailBody = new StringBuilder();
//			if (user.getFirstName() != null) {
//				mailBody.append("Dear " + user.getFirstName() + ",");
//			} else {
//				mailBody.append("Dear User,");
//			}
			mailBody.append(System.lineSeparator());
//			if (deviceList) {
			mailBody.append(body);
//			} else {
//				mailBody.append("Please Download a file using below url");
//			}
			mailBody.append(System.lineSeparator());
			mailBody.append(System.lineSeparator());
			mailBody.append("URL: " + url + "device/download?fileName=" + fileName);
			mailBody.append(System.lineSeparator());
			mailBody.append(System.lineSeparator());
			mailBody.append(
					"Please note that this link will expire after 24 hours, after which the export file will no longer be available.");
			mailBody.append(System.lineSeparator());
			mailBody.append(System.lineSeparator());
			mailBody.append("PCT Support");
			message.setFrom("donotreply@phillips-connect.com");

			helper.setTo(user.getEmail());
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
	
	public String sendMailForAssetAssociation(List<AssociationPayload> unAssociationPayload,String email) {
		MimeMessage message = sender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		try {
			String subject = "Exceptions Found When Importing Asset Data";
			String body = "The following exceptions were found while importing Asset details.  Please review  to see if manual updates are required.";
			logger.info("Prepare mail object");
			StringBuilder mailBody = new StringBuilder();

			mailBody.append("<br>");
			mailBody.append(body);

			mailBody.append("<br>");
			mailBody.append("<br>");
			mailBody.append("The list of exceptions will be embedded in the body of the email:");
			mailBody.append("<br>");
			mailBody.append("<br>");
			
			mailBody.append("<html><body><table width='100%' border='1'><tr><td><b>IMEI</b></td><td><b>Asset ID</b></td><td><b>Asset Type</b></td></tr>");
			for (AssociationPayload associationPayload : unAssociationPayload) {
				mailBody.append("<tr><td>");
				mailBody.append(associationPayload.getDeviceId());
				mailBody.append("</td><td>");
				mailBody.append( associationPayload.getAssetId());
				mailBody.append("</td><td>");
				mailBody.append(associationPayload.getAssetType());
				mailBody.append("</td></tr>");
			}
			mailBody.append("</table></body></html>");
			mailBody.append("<br>");
			mailBody.append("<br>");
			mailBody.append("PCT Support");
			message.setFrom("donotreply@phillips-connect.com");

			helper.setTo("kstruble@phillips-connect.com");
			helper.setCc(email);
		//	helper.setTo(email);
			helper.setSubject(subject);

//			BodyPart messageBodyPart1 = new MimeBodyPart();
//			messageBodyPart1.setText(new String(mailBody));
//			Multipart multipart = new MimeMultipart();
//			multipart.addBodyPart(messageBodyPart1);
			message.setContent(new String(mailBody),"text/html");
			sender.send(message);
			logger.info("Mail send Success!");
		} catch (Exception e) {
			logger.error("Error", e);
			return "Error while sending mail ..";
		}
		return "Mail Sent Success!";
	}
}