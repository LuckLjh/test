package com.cesgroup.api.user;

import com.cesgroup.core.util.WorkflowConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 账户状态
 * 
 * @author 国栋
 *
 */
public class AccountStatus {

    private static Logger logger = LoggerFactory.getLogger(AccountStatus.class);

    // status
    /** otpPasswordTooShort */
    public static final String OTP_PASSWORD_TOO_SHORT = "otpPasswordTooShort";

    /** otpNotExists */
    public static final String OTP_NOT_EXISTS = "otpNotExists";

    /** otpStatusInvalid */
    public static final String OTP_STATUS_INVALID = "otpStatusInvalid";

    /** otpCodeFormatInvalid */
    public static final String OTP_CODE_FORMAT_INVALID = "otpCodeFormatInvalid";

    /** otpCodeExpired */
    public static final String OTP_CODE_EXPIRED = "otpCodeExpired";

    /** otpCodeInvalid */
    public static final String OTP_CODE_INVALID = "otpCodeInvalid";

    /** badCredentials */
    public static final String BAD_CREDENTIALS = "badCredentials";

    /** badHours */
    public static final String BAD_HOURS = "badHours";

    /** badWorkstation */
    public static final String BAD_WORKSTATION = "badWorkstation";

    /** accountNotExists */
    public static final String ACCOUNT_NOT_EXISTS = "accountNotExists";

    /** passwordNotExists */
    public static final String PASSWORD_NOT_EXISTS = "passwordNotExists";

    /** accountLocked */
    public static final String ACCOUNT_LOCKED = "accountLocked";

    /** accountExpired */
    public static final String ACCOUNT_EXPIRED = "accountExpired";

    /** accountDisabled */
    public static final String ACCOUNT_DISABLED = "accountDisabled";

    /** passwordExpired */
    public static final String PASSWORD_EXPIRED = "passwordExpired";

    /** passwordMustChange */
    public static final String PASSWORD_MUST_CHANGE = "passwordMustChange";

    /** enabled */
    public static final String ENABLED = "enabled";

    /** locked */
    public static final String LOCKED = "locked";

    // result
    /** success */
    public static final String SUCCESS = "success";

    /** failure */
    public static final String FAILURE = "failure";

    // success reason
    /** otp */
    public static final String OTP = "otp";

    /** normal */
    public static final String NORMAL = WorkflowConstants.HumanTaskConstants.CATALOG_NORMAL;

    /** special */
    public static final String SPECIAL = "special";

    /**
     * 转换异常信息
     * 
     * @param exceptionMessage
     *            异常信息
     * @return String
     */
    public static String convertLdapException(String exceptionMessage) {
        if (exceptionMessage.indexOf("data 525") != -1) {
            return AccountStatus.ACCOUNT_NOT_EXISTS;
        } else if (exceptionMessage.indexOf("data 52e") != -1) {
            logger.info(exceptionMessage);

            return AccountStatus.BAD_CREDENTIALS;
        } else if (exceptionMessage.indexOf("data 530") != -1) {
            return AccountStatus.BAD_HOURS;
        } else if (exceptionMessage.indexOf("data 531") != -1) {
            return AccountStatus.BAD_WORKSTATION;
        } else if (exceptionMessage.indexOf("data 532") != -1) {
            return AccountStatus.PASSWORD_EXPIRED;
        } else if (exceptionMessage.indexOf("data 533") != -1) {
            return AccountStatus.ACCOUNT_DISABLED;
        } else if (exceptionMessage.indexOf("data 701") != -1) {
            return AccountStatus.ACCOUNT_EXPIRED;
        } else if (exceptionMessage.indexOf("data 733") != -1) {
            return AccountStatus.PASSWORD_MUST_CHANGE;
        } else if (exceptionMessage.indexOf("data 775") != -1) {
            return AccountStatus.ACCOUNT_LOCKED;
        } else {
            return exceptionMessage;
        }
    }
}
