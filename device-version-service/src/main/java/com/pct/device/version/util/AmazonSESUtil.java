package com.pct.device.version.util;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AmazonSESUtil {
	private static final Logger logger = LoggerFactory.getLogger(AmazonSESUtil.class);

	@Value("${aws.ses.smtp.username}")
	String smtpUsername;

	@Value("${aws.ses.smtp.password}")
	String smtpPassword;

	@Value("${aws.ses.smtp.host}")
	String smtpHost;

	@Value("${aws.ses.smtp.port}")
	String smtpPort;

	@Value("${aws.ses.smtp.email.from}")
	String smtpEmailFrom;

	@Value("${aws.ses.smtp.email.to}")
	String smtpEmailTo;

	@Value("${aws.ses.smtp.email.fromname}")
	String smtpEmailFromName;

	public void sendEmail(String imei, String campaignName)
			throws UnsupportedEncodingException, MessagingException {
		logger.info("Email sending  imei: " + imei +" campaignName: "+campaignName );

		String subject_device_problem_status = "Device with " + imei + " has been assigned to Problem status";
		
		String body_device_problem_status = String.join(System.getProperty("line.separator"), "<p>The device with IMEI "
				+ imei + " # in " + campaignName
				+ " has been assigned the Problem status. After you have resolved the issue, you can reset this status in the Gateways tab of the campaign.");

		// Create a Properties object to contain connection configuration information.
		Properties props = System.getProperties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.port", smtpPort);
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.auth", "true");

		// Create a Session object to represent a mail session with the specified
		// properties.
		Session session = Session.getDefaultInstance(props);

		// Create a message with the specified information.
		MimeMessage msg = new MimeMessage(session);
		msg.setFrom(new InternetAddress(smtpEmailFrom, smtpEmailFromName));
		msg.setRecipient(Message.RecipientType.TO, new InternetAddress(smtpEmailTo));
		msg.setSubject(subject_device_problem_status);
		msg.setContent(body_device_problem_status, "text/html");

		// Add a configuration set header. Comment or delete the
		// next line if you are not using a configuration set
		// msg.setHeader("X-SES-CONFIGURATION-SET", CONFIGSET);

		// Create a transport.
		Transport transport = session.getTransport();
		// Send the message.
		try {

			// Connect to Amazon SES using the SMTP username and password you specified
			// above.
			transport.connect(smtpHost, smtpUsername, smtpPassword);

			// Send the email.
			transport.sendMessage(msg, msg.getAllRecipients());
			logger.info("Email sent  " );
		} catch (Exception ex) {
			ex.printStackTrace();

		} finally {
			// Close and terminate the connection.
			transport.close();
		}

	}

}