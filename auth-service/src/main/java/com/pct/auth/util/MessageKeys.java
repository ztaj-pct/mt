package com.pct.auth.util;

import org.springframework.stereotype.Component;

@Component
public class MessageKeys {

	public static final String DUPLICATE_USER_NAME = "fpgateway.username.duplicate";
	public static final String DUPLICATE_EMAIL_ID = "fpgateway.email.duplicate";
	public static final String DUPLICATE_MOBILE_NUMBER = "fpgateway.mobile.duplicate";
	public static final String USER_ADDED_SUCCESSFULLY = "fpgateway.user.added";
	public static final String INTERNAL_SERVER_ERROR = "fpgateway.error.internal.server";
	public static final String INVALID_CREDENTIAL = "fpgateway.error.invalid.credential";
	public static final String FORGOT_PASSWORD_MAIL_SENT = "fpgateway.forgot.password.mailsent";
	public static final String EMAIL_NOT_ASSOCIALTE = "fpgateway.email.not.associate";
	public static final String NULL_USER_ID = "fpgateway.error.user.id";
	public static final String USER_UPDATED_SUCCESSFULLY = "fpgateway.user.updated.success";
	public static final String EMAIL_FETCHED_ERROR = "fpgateway.error.emailNotFetched";
	public static final String EMAIL_FETCHED_SUCCESSFULLY = "fpgateway.success.emailFetched";
	public static final String EMAIL_NOT_FOUND = "fpgateway.error.email.notFound";
	public static final String USER_NAME_FETCHED_SUCCESSFULLY = "fpgateway.success.userNameFetched";
	public static final String USER_NAME_NOT_FOUND = "fpgateway.error.userName.notFound";
	public static final String USER_NAME_FETCHED_ERROR = "fpgateway.error.userNameNotFetched";

	public static final String USER_FETCHED_SUCCESSFULLY = "fpgateway.success.user.Fetched";
	public static final String USER_NOT_FOUND = "fpgateway.error.userId.notFound";

	public static final String INVALID_USER = "fpgateway.error.user.invalid";
	public static final String USER_DELETE_SUCCESSFULLY = "fpgateway.success.user.delete";
	public static final String INCORRECT_EXISITING_PASSWORD = "fpgateway.error.password.incorrect";
	public static final String PASSWORD_RESET_SUCCESSFULLY = "fpgateway.success.password.reset";

	public static final String COMPNAY_ADDEED_SUCCESSFULLY = "fpgateway.success.company.saved";
	public static final String INVALID_REQUEST = "fpgateway.error.invalid.request";
	public static final String COMPANY_FETCHED_SUCCESSFULLY = "fpgateway.success.company.fetched";
	public static final String COMPANY_DELETED_SUCCESSFULLY = "fpgateway.success.company.delete";
	public static final String COMPANY_NOT_FOUND = "fpgateway.error.company.notFound";
	public static final String ALL_COMPANY_FETCHED_SUCCESSFULLY = "fpgateway.success.allCompany.fetched";
	public static final String UNREGISTRERD_COMPANY_FETCHED_SUCCESSFULLY = "fpgateway.success.unRegistered.fetched";
	public static final String PAGE_NOT_VALID = "fpgateway.error.page.notValid";
	public static final String DEVICE_UPDATED_SUCCESSFULLY = "fpgateway.success.device.update";
	public static final String DEVICE_NOT_FOUND = "fpgateway.error.device.notFound";
	public static final String DEVICE_FOUND = "fpgateway.success.device.Found";
	public static final String ASSETS_FOUND = "Assets added ";
	public static final String ALL_DEVICE_FOUND = "fpgateway.success.alldevice.Found";
	public static final String RESOURCE_NOT_FOUND = "fpgateway.error.resource.notFound";
	public static final String RESET_PASSWORD_BY_ADMIN = "fpgateway.admin.reset.password.mailsent";
	public static final String RESET_PASSWORD_FAILED = "fpgateway.reset.password.failed";
	public static final String OTP_VERIFICATION_SUCCESSFULLY = "fpgateway.verify.otp.successful";
	public static final String OTP_VERIFICATION_FAILED = "fpgateway.verify.otp.failed";
	public static final String OTP_GENERATED_SUCCESSFULLY = "fpgateway.otp.generated.successfully";
	public static final String USER_UNAUTHORIZED = "user.error.user.unauthorized";

	public static final String FORGOT_PASSWORD_NO_USER_MOBILE = "fp.no-user.mobile";
}