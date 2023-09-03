package com.pct.auth.util;

import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.pct.common.model.User;

@Service
public class MailUtil {
	private static final Logger logger = LoggerFactory.getLogger(MailUtil.class);

	@Autowired(required = true)
	private JavaMailSender sender;

	@Value("${endpoint.url}")
	private String url;

	public String sendMail(User user, String password) {
		MimeMessage message = sender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		try {
			StringBuilder mailBody = new StringBuilder();
			mailBody.append("Dear " + user.getFirstName() + ",");
			mailBody.append(System.lineSeparator());
			mailBody.append(System.lineSeparator());
			mailBody.append(
					"A new temporary password has been generated for your PCT Cloud Tools account. You will be required to select a new password when you next log in.");
			mailBody.append(System.lineSeparator());
			mailBody.append(System.lineSeparator());
			mailBody.append("New Password: " + password);
			mailBody.append(System.lineSeparator());
			mailBody.append(System.lineSeparator());
			mailBody.append("URL: " + url);
			message.setContent(mailBody, "text/html; charset=utf-8");
			//message.setFrom("rajeshkhore01@gmail.com");
			message.setFrom("donotreply@phillips-connect.com");
			helper.setTo(user.getEmail());
			helper.setText(mailBody.toString());
			helper.setSubject("Password Reset Notification for PCT Cloud Tools");
			sender.send(message);
		} catch (Exception e) {
			logger.error("Error", e);
			return "Error while sending mail ..";
		}
		return "Mail Sent Success!";
	}
}